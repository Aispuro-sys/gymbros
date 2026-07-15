const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

router.get('/', async (req, res) => {
  try {
    const items = await prisma.supplementMed.findMany({
      where: { user_id: req.userId },
      orderBy: [{ is_medication: 'asc' }, { name: 'asc' }],
    });
    res.json({ supplements: items });
  } catch (err) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name, dosage, time_of_day, is_medication } = req.body;
    if (!name || !dosage) return res.status(400).json({ error: 'Name and dosage are required' });

    const item = await prisma.supplementMed.create({
      data: {
        user_id: req.userId,
        name,
        dosage,
        time_of_day: time_of_day || 'MORNING',
        is_medication: is_medication || false,
      },
    });
    res.status(201).json({ supplement: item });
  } catch (err) {
    console.error('Create supplement error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const item = await prisma.supplementMed.findUnique({ where: { id: req.params.id } });
    if (!item || item.user_id !== req.userId) {
      return res.status(404).json({ error: 'Supplement not found' });
    }
    await prisma.supplementMed.delete({ where: { id: item.id } });
    res.json({ message: 'Supplement deleted' });
  } catch (err) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
