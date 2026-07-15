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

module.exports = router;
