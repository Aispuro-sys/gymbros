const express = require('express');
const crypto = require('crypto');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

router.get('/', async (req, res) => {
  try {
    const memberships = await prisma.teamMember.findMany({
      where: { user_id: req.userId },
      include: {
        team: {
          include: {
            members: { include: { user: { select: { id: true, username: true } } } },
          },
        },
      },
    });
    res.json({ teams: memberships.map((m) => ({ ...m.team, role: m.role })) });
  } catch (err) {
    console.error('Get teams error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name } = req.body;
    if (!name) return res.status(400).json({ error: 'Team name is required' });

    const invite_code = 'GYM-' + crypto.randomBytes(3).toString('hex').toUpperCase();

    const team = await prisma.team.create({
      data: {
        name,
        admin_id: req.userId,
        invite_code,
        members: {
          create: { user_id: req.userId, role: 'ADMIN' },
        },
      },
      include: { members: true },
    });
    res.status(201).json({ team });
  } catch (err) {
    console.error('Create team error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/join', async (req, res) => {
  try {
    const { invite_code } = req.body;
    if (!invite_code) return res.status(400).json({ error: 'Invite code is required' });

    const team = await prisma.team.findUnique({ where: { invite_code } });
    if (!team) return res.status(404).json({ error: 'Invalid invite code' });

    const existing = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: team.id, user_id: req.userId } },
    });
    if (existing) return res.status(409).json({ error: 'Already a member' });

    const member = await prisma.teamMember.create({
      data: { team_id: team.id, user_id: req.userId, role: 'MEMBER' },
    });
    res.status(201).json({ team, member });
  } catch (err) {
    console.error('Join team error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get team detail with members, shared routines, and feed
router.get('/:id', async (req, res) => {
  try {
    const membership = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: req.params.id, user_id: req.userId } },
    });
    if (!membership) return res.status(403).json({ error: 'No perteneces a este equipo' });

    const team = await prisma.team.findUnique({
      where: { id: req.params.id },
      include: {
        members: { include: { user: { select: { id: true, username: true, goal: true, body_type: true } } } },
        shared_routines: {
          include: {
            routine: { include: { exercises: { orderBy: { order_index: 'asc' } } } },
            user: { select: { id: true, username: true } },
          },
          orderBy: { shared_at: 'desc' },
        },
        posts: {
          include: { user: { select: { id: true, username: true } } },
          orderBy: { created_at: 'desc' },
          take: 50,
        },
      },
    });
    if (!team) return res.status(404).json({ error: 'Team not found' });
    res.json({ team, role: membership.role });
  } catch (err) {
    console.error('Get team detail error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Share a routine to team
router.post('/:id/share-routine', async (req, res) => {
  try {
    const { routine_id } = req.body;
    if (!routine_id) return res.status(400).json({ error: 'Routine ID is required' });

    const membership = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: req.params.id, user_id: req.userId } },
    });
    if (!membership) return res.status(403).json({ error: 'No perteneces a este equipo' });

    const routine = await prisma.routine.findUnique({ where: { id: routine_id } });
    if (!routine || routine.user_id !== req.userId) {
      return res.status(404).json({ error: 'Rutina no encontrada' });
    }

    const shared = await prisma.teamRoutine.create({
      data: { team_id: req.params.id, routine_id, shared_by: req.userId },
    });

    await prisma.teamPost.create({
      data: {
        team_id: req.params.id,
        user_id: req.userId,
        content: `ha compartido la rutina "${routine.name}"`,
        post_type: 'ROUTINE',
        routine_id,
      },
    });

    res.status(201).json({ shared });
  } catch (err) {
    console.error('Share routine error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Copy a shared routine to your own
router.post('/:id/copy-routine/:routineId', async (req, res) => {
  try {
    const membership = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: req.params.id, user_id: req.userId } },
    });
    if (!membership) return res.status(403).json({ error: 'No perteneces a este equipo' });

    const original = await prisma.routine.findUnique({
      where: { id: req.params.routineId },
      include: { exercises: { orderBy: { order_index: 'asc' } } },
    });
    if (!original) return res.status(404).json({ error: 'Rutina no encontrada' });

    const copied = await prisma.routine.create({
      data: {
        user_id: req.userId,
        name: original.name + ' (copia)',
        ai_generated: original.ai_generated,
        exercises: {
          create: original.exercises.map((ex, i) => ({
            name: ex.name,
            sets: ex.sets,
            reps: ex.reps,
            rest_seconds: ex.rest_seconds,
            order_index: i,
          })),
        },
      },
      include: { exercises: true },
    });
    res.status(201).json({ routine: copied });
  } catch (err) {
    console.error('Copy routine error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Post to team feed
router.post('/:id/posts', async (req, res) => {
  try {
    const { content } = req.body;
    if (!content) return res.status(400).json({ error: 'Content is required' });

    const membership = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: req.params.id, user_id: req.userId } },
    });
    if (!membership) return res.status(403).json({ error: 'No perteneces a este equipo' });

    const post = await prisma.teamPost.create({
      data: { team_id: req.params.id, user_id: req.userId, content },
      include: { user: { select: { id: true, username: true } } },
    });
    res.status(201).json({ post });
  } catch (err) {
    console.error('Create post error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Leave team
router.delete('/:id/leave', async (req, res) => {
  try {
    const membership = await prisma.teamMember.findUnique({
      where: { team_id_user_id: { team_id: req.params.id, user_id: req.userId } },
    });
    if (!membership) return res.status(404).json({ error: 'No eres miembro' });

    if (membership.role === 'ADMIN') {
      const memberCount = await prisma.teamMember.count({ where: { team_id: req.params.id } });
      if (memberCount > 1) {
        return res.status(400).json({ error: 'Transfiere el liderazgo antes de salir o elimina el equipo' });
      }
      await prisma.team.delete({ where: { id: req.params.id } });
    } else {
      await prisma.teamMember.delete({ where: { id: membership.id } });
    }
    res.json({ message: 'Has salido del equipo' });
  } catch (err) {
    console.error('Leave team error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
