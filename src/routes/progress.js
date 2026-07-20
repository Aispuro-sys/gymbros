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

const MEASUREMENT_FIELDS = ['weight_logged', 'waist_cm', 'chest_cm', 'hip_cm', 'arm_cm', 'leg_cm', 'body_fat_pct'];

function pickMeasurements(body) {
  const data = {};
  for (const key of MEASUREMENT_FIELDS) {
    if (body[key] !== undefined) {
      data[key] = body[key] === null || body[key] === '' ? null : Number(body[key]);
    }
  }
  if (body.note !== undefined) data.note = body.note || null;
  return data;
}

// Get all progress/body logs for user
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

// Tracking dashboard stats: workout streak + weekly consistency + meals
router.get('/stats', async (req, res) => {
  try {
    const logs = await prisma.exerciseLog.findMany({
      where: { user_id: req.userId, completed: true },
      select: { date: true },
      orderBy: { date: 'desc' },
    });

    // Also get workout logs for additional tracking
    const workoutLogs = await prisma.workoutLog.findMany({
      where: { user_id: req.userId },
      select: { date: true, type: true },
      orderBy: { date: 'desc' },
    });

    const uniqueDates = [...new Set(logs.map(l => l.date.toISOString().slice(0, 10)))].sort().reverse();
    const dateSet = new Set(uniqueDates);

    // Also include workout log dates (WORKOUT or CARDIO type)
    const workoutDates = new Set(
      workoutLogs
        .filter(w => w.type === 'WORKOUT' || w.type === 'CARDIO')
        .map(w => w.date.toISOString().slice(0, 10))
    );
    const allActivityDates = new Set([...uniqueDates, ...workoutDates]);
    const allDatesSorted = [...allActivityDates].sort().reverse();

    // Current streak: consecutive days with activity, counting back from today
    let streak = 0;
    const cursor = new Date();
    cursor.setHours(0, 0, 0, 0);
    if (!allActivityDates.has(cursor.toISOString().slice(0, 10))) {
      cursor.setDate(cursor.getDate() - 1);
    }
    while (allActivityDates.has(cursor.toISOString().slice(0, 10))) {
      streak++;
      cursor.setDate(cursor.getDate() - 1);
    }

    // Longest streak ever
    let longestStreak = 0;
    let currentRun = 0;
    const sortedAsc = [...allDatesSorted].sort();
    let prevDate = null;
    for (const d of sortedAsc) {
      if (prevDate) {
        const diffDays = Math.round((new Date(d) - new Date(prevDate)) / 86400000);
        currentRun = diffDays === 1 ? currentRun + 1 : 1;
      } else {
        currentRun = 1;
      }
      longestStreak = Math.max(longestStreak, currentRun);
      prevDate = d;
    }

    // Weekly workout counts for the last 8 weeks (Mon-start weeks)
    const weeklyCounts = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    for (let i = 7; i >= 0; i--) {
      const weekEnd = new Date(today);
      weekEnd.setDate(today.getDate() - i * 7);
      const dow = (weekEnd.getDay() + 6) % 7; // 0 = Monday
      const weekStart = new Date(weekEnd);
      weekStart.setDate(weekEnd.getDate() - dow);
      const weekStartStr = weekStart.toISOString().slice(0, 10);
      const weekEndDate = new Date(weekStart);
      weekEndDate.setDate(weekStart.getDate() + 6);
      const weekEndStr = weekEndDate.toISOString().slice(0, 10);
      const count = [...allActivityDates].filter(d => d >= weekStartStr && d <= weekEndStr).length;
      weeklyCounts.push({ week_start: weekStartStr, count });
    }

    const workoutsThisWeek = weeklyCounts[weeklyCounts.length - 1]?.count || 0;

    // Meal stats: count meals this week and total
    const weekStartStr = weeklyCounts[weeklyCounts.length - 1]?.week_start;
    const mealsThisWeek = weekStartStr
      ? await prisma.meal.count({
          where: {
            user_id: req.userId,
            date: { gte: new Date(weekStartStr) }
          }
        })
      : 0;

    const totalMeals = await prisma.meal.count({
      where: { user_id: req.userId }
    });

    // Rest days logged
    const restDays = workoutLogs.filter(w => w.type === 'REST').length;

    res.json({
      streak,
      longest_streak: longestStreak,
      workouts_this_week: workoutsThisWeek,
      total_workout_days: allActivityDates.size,
      weekly_counts: weeklyCounts,
      meals_this_week: mealsThisWeek,
      total_meals: totalMeals,
      rest_days: restDays,
    });
  } catch (err) {
    console.error('Get progress stats error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Create a new body log entry: photo, weight, and/or measurements — all optional except at least one value
router.post('/', async (req, res) => {
  try {
    const { photo_url } = req.body;
    const measurements = pickMeasurements(req.body);

    let savedUrl = null;
    if (photo_url) {
      savedUrl = saveBase64Image(photo_url);
      if (!savedUrl) {
        return res.status(400).json({ error: 'Invalid image format' });
      }
    }

    const hasAnyValue = savedUrl || Object.values(measurements).some(v => v !== undefined && v !== null);
    if (!hasAnyValue) {
      return res.status(400).json({ error: 'Provide a photo, weight, or at least one measurement' });
    }

    const photo = await prisma.progressPhoto.create({
      data: {
        user_id: req.userId,
        photo_url: savedUrl,
        ...measurements,
      },
    });

    res.status(201).json({ photo: { ...photo, photo_url: toFullUrl(req, photo.photo_url) } });
  } catch (err) {
    console.error('Create progress log error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete a progress/body log
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

// Update weight/measurements on a body log
router.put('/:id', async (req, res) => {
  try {
    const measurements = pickMeasurements(req.body);
    const photo = await prisma.progressPhoto.findUnique({
      where: { id: req.params.id },
    });
    if (!photo || photo.user_id !== req.userId) {
      return res.status(404).json({ error: 'Photo not found' });
    }

    const updated = await prisma.progressPhoto.update({
      where: { id: photo.id },
      data: measurements,
    });
    res.json({ photo: { ...updated, photo_url: toFullUrl(req, updated.photo_url) } });
  } catch (err) {
    console.error('Update progress photo error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ===== Workout Log endpoints =====

// Get all workout logs for user
router.get('/workouts', async (req, res) => {
  try {
    const logs = await prisma.workoutLog.findMany({
      where: { user_id: req.userId },
      orderBy: { date: 'desc' },
    });
    res.json({ logs });
  } catch (err) {
    console.error('Get workout logs error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Create or update a workout log entry (upsert by date)
router.post('/workouts', async (req, res) => {
  try {
    const { type, duration_min, intensity, notes, date } = req.body;
    if (!type || !['WORKOUT', 'REST', 'CARDIO'].includes(type)) {
      return res.status(400).json({ error: 'Invalid type. Must be WORKOUT, REST, or CARDIO' });
    }

    const logDate = date ? new Date(date) : new Date();
    logDate.setHours(0, 0, 0, 0);

    const log = await prisma.workoutLog.upsert({
      where: {
        user_id_date: {
          user_id: req.userId,
          date: logDate,
        },
      },
      create: {
        user_id: req.userId,
        date: logDate,
        type,
        duration_min: duration_min ? Number(duration_min) : null,
        intensity: intensity || null,
        notes: notes || null,
      },
      update: {
        type,
        duration_min: duration_min ? Number(duration_min) : null,
        intensity: intensity || null,
        notes: notes || null,
      },
    });

    res.status(201).json({ log });
  } catch (err) {
    console.error('Create workout log error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete a workout log
router.delete('/workouts/:id', async (req, res) => {
  try {
    const log = await prisma.workoutLog.findUnique({
      where: { id: req.params.id },
    });
    if (!log || log.user_id !== req.userId) {
      return res.status(404).json({ error: 'Workout log not found' });
    }
    await prisma.workoutLog.delete({ where: { id: log.id } });
    res.json({ message: 'Workout log deleted' });
  } catch (err) {
    console.error('Delete workout log error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
