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
    const { name, ai_generated, exercises, day_of_week } = req.body;
    if (!name) return res.status(400).json({ error: 'Routine name is required' });

    const routine = await prisma.routine.create({
      data: {
        user_id: req.userId,
        name,
        ai_generated: ai_generated || false,
        day_of_week: day_of_week || null,
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

// Toggle exercise completion
router.post('/:routineId/exercises/:exerciseId/toggle', async (req, res) => {
  try {
    const routine = await prisma.routine.findUnique({ where: { id: req.params.routineId } });
    if (!routine || routine.user_id !== req.userId) {
      return res.status(404).json({ error: 'Routine not found' });
    }

    const exercise = await prisma.exercise.findUnique({ where: { id: req.params.exerciseId } });
    if (!exercise || exercise.routine_id !== routine.id) {
      return res.status(404).json({ error: 'Exercise not found' });
    }

    const today = new Date();
    const startOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const endOfDay = new Date(startOfDay);
    endOfDay.setDate(endOfDay.getDate() + 1);

    const existing = await prisma.exerciseLog.findFirst({
      where: {
        user_id: req.userId,
        exercise_id: exercise.id,
        date: { gte: startOfDay, lt: endOfDay },
      },
    });

    if (existing) {
      const updated = await prisma.exerciseLog.update({
        where: { id: existing.id },
        data: { completed: !existing.completed },
      });
      res.json({ log: updated });
    } else {
      const log = await prisma.exerciseLog.create({
        data: {
          user_id: req.userId,
          exercise_id: exercise.id,
          routine_id: routine.id,
          completed: true,
        },
      });
      res.status(201).json({ log });
    }
  } catch (err) {
    console.error('Toggle exercise error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get completed exercises for today
router.get('/completed-today', async (req, res) => {
  try {
    const today = new Date();
    const startOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const endOfDay = new Date(startOfDay);
    endOfDay.setDate(endOfDay.getDate() + 1);

    const logs = await prisma.exerciseLog.findMany({
      where: {
        user_id: req.userId,
        date: { gte: startOfDay, lt: endOfDay },
        completed: true,
      },
      select: { exercise_id: true, routine_id: true },
    });
    res.json({ completed: logs.map((l) => l.exercise_id) });
  } catch (err) {
    console.error('Get completed error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
