const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

router.get('/', async (req, res) => {
  try {
    const routines = await prisma.routine.findMany({
      where: { user_id: req.userId },
      include: { exercises: { orderBy: { order_index: 'asc' } } },
      orderBy: { created_at: 'desc' },
    });
    res.json({ routines });
  } catch (err) {
    console.error('Get routines error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name, ai_generated, exercises } = req.body;
    if (!name) return res.status(400).json({ error: 'Routine name is required' });

    const routine = await prisma.routine.create({
      data: {
        user_id: req.userId,
        name,
        ai_generated: ai_generated || false,
        exercises: exercises
          ? {
              create: exercises.map((ex, i) => ({
                name: ex.name,
                sets: ex.sets || 3,
                reps: ex.reps || '8-12',
                rest_seconds: ex.rest_seconds || 90,
                order_index: i,
              })),
            }
          : undefined,
      },
      include: { exercises: true },
    });
    res.status(201).json({ routine });
  } catch (err) {
    console.error('Create routine error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.put('/:id', async (req, res) => {
  try {
    const { name, exercises } = req.body;
    const routine = await prisma.routine.findUnique({ where: { id: req.params.id } });
    if (!routine || routine.user_id !== req.userId) {
      return res.status(404).json({ error: 'Routine not found' });
    }

    if (exercises) {
      await prisma.exercise.deleteMany({ where: { routine_id: routine.id } });
      await prisma.exercise.createMany({
        data: exercises.map((ex, i) => ({
          routine_id: routine.id,
          name: ex.name,
          sets: ex.sets || 3,
          reps: ex.reps || '8-12',
          rest_seconds: ex.rest_seconds || 90,
          order_index: i,
        })),
      });
    }

    const updated = await prisma.routine.update({
      where: { id: routine.id },
      data: { ...(name && { name }) },
      include: { exercises: { orderBy: { order_index: 'asc' } } },
    });
    res.json({ routine: updated });
  } catch (err) {
    console.error('Update routine error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const routine = await prisma.routine.findUnique({ where: { id: req.params.id } });
    if (!routine || routine.user_id !== req.userId) {
      return res.status(404).json({ error: 'Routine not found' });
    }
    await prisma.routine.delete({ where: { id: routine.id } });
    res.json({ message: 'Routine deleted' });
  } catch (err) {
    console.error('Delete routine error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
