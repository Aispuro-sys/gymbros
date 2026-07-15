const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

// ===== Content Moderation =====
const FORBIDDEN_KEYWORDS = [
  'porn', 'xxx', 'nsfw', 'nude', 'sex', 'sexual', 'explicit',
  'cp ', 'child porn', 'underage', 'minor nude',
  'escort', 'prostitute', 'hooker',
  ' Gore', 'decapit', 'mutilat', 'torture',
  'terrorist', 'bomb instruction', 'weapon blueprint',
  'drug deal', 'sell drugs', 'cocaine sell', 'meth recipe',
];

function moderateContent(text) {
  if (!text) return { safe: true };
  const lower = text.toLowerCase();
  for (const kw of FORBIDDEN_KEYWORDS) {
    if (lower.includes(kw)) {
      return { safe: false, reason: `Contenido bloqueado: contiene "${kw.trim()}"` };
    }
  }
  return { safe: true };
}

// ===== Profile Photo Upload =====
router.put('/profile-photo', async (req, res) => {
  try {
    const { photo } = req.body;
    if (!photo) return res.status(400).json({ error: 'No photo provided' });

    const mod = moderateContent(photo);
    if (!mod.safe) return res.status(403).json({ error: mod.reason });

    const updated = await prisma.user.update({
      where: { id: req.userId },
      data: { profile_photo: photo },
      select: { id: true, username: true, profile_photo: true },
    });
    res.json({ user: updated });
  } catch (err) {
    console.error('Profile photo error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Get Community Feed =====
router.get('/feed', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = 20;
    const skip = (page - 1) * limit;

    const posts = await prisma.communityPost.findMany({
      where: { parent_id: null },
      orderBy: { created_at: 'desc' },
      skip,
      take: limit,
      include: {
        user: {
          select: { id: true, username: true, profile_photo: true, role: true },
        },
        replies: {
          include: {
            user: { select: { id: true, username: true, profile_photo: true, role: true } },
            reactions: true,
          },
          orderBy: { created_at: 'asc' },
        },
        reactions: true,
      },
    });

    res.json({ posts });
  } catch (err) {
    console.error('Community feed error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Create Post =====
router.post('/posts', async (req, res) => {
  try {
    const { content, media_url, media_type, routine_id } = req.body;
    if (!content && !media_url) {
      return res.status(400).json({ error: 'Post must have content or media' });
    }

    const mod = moderateContent(content);
    if (!mod.safe) return res.status(403).json({ error: mod.reason });

    if (media_url) {
      const mediaMod = moderateContent(media_url);
      if (!mediaMod.safe) return res.status(403).json({ error: mediaMod.reason });
    }

    const post = await prisma.communityPost.create({
      data: {
        user_id: req.userId,
        content: content || '',
        media_url: media_url || null,
        media_type: media_type || 'TEXT',
        routine_id: routine_id || null,
      },
      include: {
        user: { select: { id: true, username: true, profile_photo: true, role: true } },
        reactions: true,
        replies: {
          include: {
            user: { select: { id: true, username: true, profile_photo: true, role: true } },
          },
        },
      },
    });

    res.status(201).json({ post });
  } catch (err) {
    console.error('Create post error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Reply to Post =====
router.post('/posts/:id/replies', async (req, res) => {
  try {
    const { content, media_url } = req.body;
    if (!content && !media_url) {
      return res.status(400).json({ error: 'Reply must have content or media' });
    }

    const mod = moderateContent(content);
    if (!mod.safe) return res.status(403).json({ error: mod.reason });

    const reply = await prisma.communityReply.create({
      data: {
        post_id: req.params.id,
        user_id: req.userId,
        content: content || '',
        media_url: media_url || null,
      },
      include: {
        user: { select: { id: true, username: true, profile_photo: true, role: true } },
      },
    });

    res.status(201).json({ reply });
  } catch (err) {
    console.error('Create reply error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== React to Post =====
router.post('/posts/:id/react', async (req, res) => {
  try {
    const { emoji } = req.body;
    if (!emoji) return res.status(400).json({ error: 'Emoji required' });

    const existing = await prisma.communityReaction.findFirst({
      where: { post_id: req.params.id, user_id: req.userId, emoji },
    });

    if (existing) {
      await prisma.communityReaction.delete({ where: { id: existing.id } });
      return res.json({ reacted: false });
    }

    await prisma.communityReaction.create({
      data: { post_id: req.params.id, user_id: req.userId, emoji },
    });
    res.json({ reacted: true });
  } catch (err) {
    console.error('React error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Delete Post (owner or moderator) =====
router.delete('/posts/:id', async (req, res) => {
  try {
    const post = await prisma.communityPost.findUnique({ where: { id: req.params.id } });
    if (!post) return res.status(404).json({ error: 'Post not found' });

    const user = await prisma.user.findUnique({ where: { id: req.userId } });
    if (post.user_id !== req.userId && user.role !== 'MODERATOR' && user.role !== 'ADMIN') {
      return res.status(403).json({ error: 'Not authorized to delete this post' });
    }

    await prisma.communityPost.delete({ where: { id: req.params.id } });
    res.json({ success: true });
  } catch (err) {
    console.error('Delete post error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Get User Profile =====
router.get('/profile/:userId', async (req, res) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.params.userId },
      select: {
        id: true,
        username: true,
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

    const postCount = await prisma.communityPost.count({ where: { user_id: req.params.userId } });
    const routineCount = await prisma.routine.count({ where: { user_id: req.params.userId } });

    res.json({ user: { ...user, post_count: postCount, routine_count: routineCount } });
  } catch (err) {
    console.error('Profile fetch error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Search Users =====
router.get('/users/search', async (req, res) => {
  try {
    const { q } = req.query;
    if (!q || q.length < 2) return res.json({ users: [] });

    const users = await prisma.user.findMany({
      where: { username: { contains: q, mode: 'insensitive' } },
      select: { id: true, username: true, profile_photo: true, role: true, bio: true },
      take: 20,
    });
    res.json({ users });
  } catch (err) {
    console.error('User search error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Update Role (self only for bio, admin for role) =====
router.put('/role', async (req, res) => {
  try {
    const { role } = req.body;
    const validRoles = ['NORMAL', 'MODERATOR', 'ATHLETE'];
    if (!validRoles.includes(role)) {
      return res.status(400).json({ error: 'Invalid role' });
    }

    const requester = await prisma.user.findUnique({ where: { id: req.userId } });
    if (requester.role !== 'ADMIN' && role !== 'NORMAL') {
      return res.status(403).json({ error: 'Only admins can assign roles' });
    }

    const updated = await prisma.user.update({
      where: { id: req.userId },
      data: { role },
      select: { id: true, username: true, role: true },
    });
    res.json({ user: updated });
  } catch (err) {
    console.error('Role update error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
