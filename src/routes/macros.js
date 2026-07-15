const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

router.get('/', async (req, res) => {
  try {
    const { date } = req.query;
    const where = { user_id: req.userId };
    if (date) {
      const d = new Date(date);
      const next = new Date(d);
      next.setDate(next.getDate() + 1);
      where.date = { gte: d, lt: next };
    }
    const logs = await prisma.macrosDailyLog.findMany({
      where,
      orderBy: { date: 'desc' },
    });
    res.json({ logs });
  } catch (err) {
    console.error('Get macros error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { date, calories, protein_g, carbs_g, fats_g } = req.body;
    if (!date) return res.status(400).json({ error: 'Date is required' });

    const d = new Date(date);
    const next = new Date(d);
    next.setDate(next.getDate() + 1);

    const existing = await prisma.macrosDailyLog.findFirst({
      where: { user_id: req.userId, date: { gte: d, lt: next } },
    });

    if (existing) {
      const updated = await prisma.macrosDailyLog.update({
        where: { id: existing.id },
        data: {
          calories: calories ?? existing.calories,
          protein_g: protein_g ?? existing.protein_g,
          carbs_g: carbs_g ?? existing.carbs_g,
          fats_g: fats_g ?? existing.fats_g,
        },
      });
      return res.json({ log: updated });
    }

    const log = await prisma.macrosDailyLog.create({
      data: {
        user_id: req.userId,
        date: d,
        calories: calories || 0,
        protein_g: protein_g || 0,
        carbs_g: carbs_g || 0,
        fats_g: fats_g || 0,
      },
    });
    res.status(201).json({ log });
  } catch (err) {
    console.error('Create macros error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Weekly nutrition summary
router.get('/weekly-summary', async (req, res) => {
  try {
    const today = new Date();
    const startOfWeek = new Date(today);
    startOfWeek.setDate(today.getDate() - 6);
    startOfWeek.setHours(0, 0, 0, 0);

    const [macrosLogs, meals] = await Promise.all([
      prisma.macrosDailyLog.findMany({
        where: { user_id: req.userId, date: { gte: startOfWeek } },
        orderBy: { date: 'desc' },
      }),
      prisma.meal.findMany({
        where: { user_id: req.userId, date: { gte: startOfWeek } },
        orderBy: { date: 'desc' },
      }),
    ]);

    const byDay = {};
    for (let i = 0; i < 7; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() - i);
      const key = d.toISOString().split('T')[0];
      byDay[key] = {
        date: key,
        calories: 0,
        protein_g: 0,
        carbs_g: 0,
        fats_g: 0,
        meals_total: 0,
        meals_confirmed: 0,
        meals_unconfirmed: 0,
      };
    }

    macrosLogs.forEach((log) => {
      const key = new Date(log.date).toISOString().split('T')[0];
      if (byDay[key]) {
        byDay[key].calories += log.calories || 0;
        byDay[key].protein_g += log.protein_g || 0;
        byDay[key].carbs_g += log.carbs_g || 0;
        byDay[key].fats_g += log.fats_g || 0;
      }
    });

    meals.forEach((meal) => {
      const key = new Date(meal.date).toISOString().split('T')[0];
      if (byDay[key]) {
        byDay[key].meals_total++;
        if (meal.confirmed) {
          byDay[key].meals_confirmed++;
        } else {
          byDay[key].meals_unconfirmed++;
          byDay[key].calories += meal.calories || 0;
          byDay[key].protein_g += meal.protein_g || 0;
          byDay[key].carbs_g += meal.carbs_g || 0;
          byDay[key].fats_g += meal.fats_g || 0;
        }
      }
    });

    const days = Object.values(byDay).sort((a, b) => b.date.localeCompare(a.date));
    const avgCalories = Math.round(days.reduce((s, d) => s + d.calories, 0) / 7);
    const totalUnconfirmed = days.reduce((s, d) => s + d.meals_unconfirmed, 0);
    const totalMeals = days.reduce((s, d) => s + d.meals_total, 0);

    res.json({
      avgCalories,
      totalMeals,
      totalUnconfirmed,
      days,
    });
  } catch (err) {
    console.error('Weekly summary error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
