const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.post('/register', async (req, res) => {
  try {
    const { username, email, password, age, height_cm, weight_kg, goal, body_type, gender } = req.body;

    if (!username || !email || !password) {
      return res.status(400).json({ error: 'Username, email and password are required' });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const existing = await prisma.user.findFirst({
      where: { OR: [{ email: normalizedEmail }, { username }] },
    });
    if (existing) {
      return res.status(409).json({ error: 'User with that email or username already exists' });
    }

    const hashed = await bcrypt.hash(password, 10);
    const user = await prisma.user.create({
      data: {
        username,
        email: normalizedEmail,
        password: hashed,
        age: age || null,
        height_cm: height_cm || null,
        weight_kg: weight_kg || null,
        goal: goal || 'MAINTENANCE',
        body_type: body_type || null,
        gender: gender || 'M',
      },
    });

    const token = jwt.sign({ userId: user.id }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.status(201).json({
      token,
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        age: user.age,
        height_cm: user.height_cm,
        weight_kg: user.weight_kg,
        goal: user.goal,
        body_type: user.body_type,
        gender: user.gender,
        role: user.role,
        bio: user.bio,
        profile_photo: user.profile_photo,
      },
    });
  } catch (err) {
    console.error('Register error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required' });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const user = await prisma.user.findUnique({ where: { email: normalizedEmail } });
    if (!user) {
      console.log('Login failed: user not found for email:', normalizedEmail);
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const valid = await bcrypt.compare(password, user.password);
    if (!valid) {
      console.log('Login failed: password mismatch for user:', user.email);
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const token = jwt.sign({ userId: user.id }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.json({
      token,
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        age: user.age,
        height_cm: user.height_cm,
        weight_kg: user.weight_kg,
        goal: user.goal,
        body_type: user.body_type,
        gender: user.gender,
        role: user.role,
        bio: user.bio,
        profile_photo: user.profile_photo,
      },
    });
  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.get('/me', authMiddleware, async (req, res) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: {
        id: true,
        username: true,
        email: true,
        phone: true,
        age: true,
        height_cm: true,
        weight_kg: true,
        goal: true,
        body_type: true,
        gender: true,
        role: true,
        bio: true,
        profile_photo: true,
        created_at: true,
      },
    });
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ user });
  } catch (err) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.put('/profile', authMiddleware, async (req, res) => {
  try {
    const { username, age, height_cm, weight_kg, goal, body_type, gender, bio, profile_photo, phone } = req.body;
    const updated = await prisma.user.update({
      where: { id: req.userId },
      data: {
        ...(username && { username }),
        ...(age !== undefined && { age }),
        ...(height_cm !== undefined && { height_cm }),
        ...(weight_kg !== undefined && { weight_kg }),
        ...(goal && { goal }),
        ...(body_type !== undefined && { body_type }),
        ...(gender !== undefined && { gender }),
        ...(bio !== undefined && { bio }),
        ...(profile_photo !== undefined && { profile_photo }),
        ...(phone !== undefined && { phone }),
      },
      select: {
        id: true,
        username: true,
        email: true,
        phone: true,
        age: true,
        height_cm: true,
        weight_kg: true,
        goal: true,
        body_type: true,
        gender: true,
        role: true,
        bio: true,
        profile_photo: true,
      },
    });
    res.json({ user: updated });
  } catch (err) {
    console.error('Profile update error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
