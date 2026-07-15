const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

// Get meals for today (or specific date)
router.get('/', async (req, res) => {
  try {
    const { date } = req.query;
    const targetDate = date ? new Date(date) : new Date();
    const startOfDay = new Date(targetDate.getFullYear(), targetDate.getMonth(), targetDate.getDate());
    const endOfDay = new Date(startOfDay);
    endOfDay.setDate(endOfDay.getDate() + 1);

    const meals = await prisma.meal.findMany({
      where: {
        user_id: req.userId,
        date: { gte: startOfDay, lt: endOfDay },
      },
      orderBy: { created_at: 'asc' },
    });

    const totals = meals.reduce((acc, m) => ({
      calories: acc.calories + m.calories,
      protein_g: acc.protein_g + m.protein_g,
      carbs_g: acc.carbs_g + m.carbs_g,
      fats_g: acc.fats_g + m.fats_g,
    }), { calories: 0, protein_g: 0, carbs_g: 0, fats_g: 0 });

    res.json({ meals, totals });
  } catch (err) {
    console.error('Get meals error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Helper: recalculate daily macros log from all meals of the day
async function syncDailyMacros(userId, targetDate) {
  const startOfDay = new Date(targetDate.getFullYear(), targetDate.getMonth(), targetDate.getDate());
  const endOfDay = new Date(startOfDay);
  endOfDay.setDate(endOfDay.getDate() + 1);

  const meals = await prisma.meal.findMany({
    where: { user_id: userId, date: { gte: startOfDay, lt: endOfDay } },
  });

  const totals = meals.reduce((acc, m) => ({
    calories: acc.calories + m.calories,
    protein_g: acc.protein_g + m.protein_g,
    carbs_g: acc.carbs_g + m.carbs_g,
    fats_g: acc.fats_g + m.fats_g,
  }), { calories: 0, protein_g: 0, carbs_g: 0, fats_g: 0 });

  const existing = await prisma.macrosDailyLog.findFirst({
    where: { user_id: userId, date: { gte: startOfDay, lt: endOfDay } },
  });

  if (existing) {
    await prisma.macrosDailyLog.update({
      where: { id: existing.id },
      data: totals,
    });
  } else {
    await prisma.macrosDailyLog.create({
      data: { user_id: userId, date: startOfDay, ...totals },
    });
  }
}

// Create a meal
router.post('/', async (req, res) => {
  try {
    const { name, meal_type, calories, protein_g, carbs_g, fats_g, photo_url } = req.body;
    if (!name) return res.status(400).json({ error: 'Meal name is required' });

    const meal = await prisma.meal.create({
      data: {
        user_id: req.userId,
        name,
        meal_type: meal_type || 'SNACK',
        calories: calories || 0,
        protein_g: protein_g || 0,
        carbs_g: carbs_g || 0,
        fats_g: fats_g || 0,
        photo_url: photo_url || null,
        confirmed: !!photo_url,
      },
    });

    await syncDailyMacros(req.userId, new Date());

    res.status(201).json({ meal });
  } catch (err) {
    console.error('Create meal error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Confirm a meal with photo
router.put('/:id/confirm', async (req, res) => {
  try {
    const { photo_url } = req.body;
    if (!photo_url) return res.status(400).json({ error: 'Photo is required' });

    const meal = await prisma.meal.findUnique({ where: { id: req.params.id } });
    if (!meal || meal.user_id !== req.userId) {
      return res.status(404).json({ error: 'Meal not found' });
    }

    const updated = await prisma.meal.update({
      where: { id: meal.id },
      data: { photo_url, confirmed: true },
    });
    res.json({ meal: updated });
  } catch (err) {
    console.error('Confirm meal error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete a meal
router.delete('/:id', async (req, res) => {
  try {
    const meal = await prisma.meal.findUnique({ where: { id: req.params.id } });
    if (!meal || meal.user_id !== req.userId) {
      return res.status(404).json({ error: 'Meal not found' });
    }
    await prisma.meal.delete({ where: { id: meal.id } });
    await syncDailyMacros(req.userId, new Date(meal.date));
    res.json({ message: 'Meal deleted' });
  } catch (err) {
    console.error('Delete meal error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
