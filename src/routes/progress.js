const express = require('express');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

const UPLOADS_DIR = path.join(__dirname, '..', 'public', 'uploads', 'progress');
if (!fs.existsSync(UPLOADS_DIR)) {
  fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

function saveBase64Image(dataUrl) {
  const matches = dataUrl.match(/^data:image\/([a-zA-Z]+);base64,(.+)$/);
  if (!matches) return null;

  const ext = matches[1] === 'jpeg' ? 'jpg' : matches[1];
  const buffer = Buffer.from(matches[2], 'base64');
  const filename = `${crypto.randomUUID()}.${ext}`;
  const filepath = path.join(UPLOADS_DIR, filename);
  fs.writeFileSync(filepath, buffer);
  return `/uploads/progress/${filename}`;
}

function deleteFile(fileUrl) {
  if (!fileUrl || !fileUrl.startsWith('/uploads/progress/')) return;
  const filepath = path.join(__dirname, '..', 'public', fileUrl);
  try { fs.unlinkSync(filepath); } catch (_) {}
}

function toFullUrl(req, url) {
  if (!url) return null;
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url;
  const baseUrl = `${req.protocol}://${req.get('host')}`;
  return baseUrl + url;
}

router.use(authMiddleware);

// Get all progress photos for user
router.get('/', async (req, res) => {
  try {
    const photos = await prisma.progressPhoto.findMany({
      where: { user_id: req.userId },
      orderBy: { date: 'desc' },
    });
    const photosWithUrls = photos.map(p => ({
      ...p,
      photo_url: toFullUrl(req, p.photo_url),
    }));
    res.json({ photos: photosWithUrls });
  } catch (err) {
    console.error('Get progress photos error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Upload a new progress photo
router.post('/', async (req, res) => {
  try {
    const { photo_url, weight_logged } = req.body;
    if (!photo_url) {
      return res.status(400).json({ error: 'Photo is required' });
    }

    const savedUrl = saveBase64Image(photo_url);
    if (!savedUrl) {
      return res.status(400).json({ error: 'Invalid image format' });
    }

    const photo = await prisma.progressPhoto.create({
      data: {
        user_id: req.userId,
        photo_url: savedUrl,
        weight_logged: weight_logged || null,
      },
    });

    res.status(201).json({ photo: { ...photo, photo_url: toFullUrl(req, photo.photo_url) } });
  } catch (err) {
    console.error('Upload progress photo error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete a progress photo
router.delete('/:id', async (req, res) => {
  try {
    const photo = await prisma.progressPhoto.findUnique({
      where: { id: req.params.id },
    });
    if (!photo || photo.user_id !== req.userId) {
      return res.status(404).json({ error: 'Photo not found' });
    }

    deleteFile(photo.photo_url);
    await prisma.progressPhoto.delete({ where: { id: photo.id } });
    res.json({ message: 'Photo deleted' });
  } catch (err) {
    console.error('Delete progress photo error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Update weight on a progress photo
router.put('/:id', async (req, res) => {
  try {
    const { weight_logged } = req.body;
    const photo = await prisma.progressPhoto.findUnique({
      where: { id: req.params.id },
    });
    if (!photo || photo.user_id !== req.userId) {
      return res.status(404).json({ error: 'Photo not found' });
    }

    const updated = await prisma.progressPhoto.update({
      where: { id: photo.id },
      data: { weight_logged: weight_logged !== undefined ? weight_logged : null },
    });
    res.json({ photo: { ...updated, photo_url: toFullUrl(req, updated.photo_url) } });
  } catch (err) {
    console.error('Update progress photo error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
