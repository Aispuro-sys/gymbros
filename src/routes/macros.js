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

module.exports = router;
