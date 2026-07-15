const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');
const { generateRoutine, generateWeeklyRoutine, generateNutritionPlan, hasOpenAI, hasGemini, hasAI, analyzeFoodPhoto, analyzeSupplementPhoto } = require('../lib/aiEngine');
const { getExerciseById } = require('../lib/exerciseDataset');

const router = express.Router();

router.use(authMiddleware);

// Validate that user has complete profile before generating AI plans
function validateProfileComplete(user) {
  const missing = [];
  if (!user.weight_kg) missing.push('peso');
  if (!user.height_cm) missing.push('altura');
  if (!user.goal) missing.push('objetivo');
  if (!user.body_type) missing.push('tipo de cuerpo');
  if (missing.length > 0) {
    return `Completa tu perfil antes de generar un plan. Faltan: ${missing.join(', ')}. Ve a la seccion Perfil.`;
  }
  return null;
}

// Generate a full weekly plan — creates multiple routines with day_of_week
router.post('/generate-weekly', async (req, res) => {
  try {
    const { days_per_week, equipment, notes } = req.body;
    const user = await prisma.user.findUnique({ where: { id: req.userId } });

    const profileError = validateProfileComplete(user);
    if (profileError) return res.status(400).json({ error: profileError });

    const params = {
      age: user.age,
      weight_kg: user.weight_kg,
      height_cm: user.height_cm,
      goal: user.goal,
      body_type: user.body_type,
      days_per_week: days_per_week || 4,
      equipment: equipment || 'all',
      notes,
    };

    const result = await generateWeeklyRoutine(params);

    // Delete old AI-generated routines for this user
    const oldRoutines = await prisma.routine.findMany({
      where: { user_id: req.userId, ai_generated: true },
    });
    for (const old of oldRoutines) {
      await prisma.routine.delete({ where: { id: old.id } });
    }

    // Create new routines for each training day
    const createdDays = [];
    for (const day of result.days) {
      const routine = await prisma.routine.create({
        data: {
          user_id: req.userId,
          name: `${day.label} - ${day.focus}`,
          ai_generated: true,
          ai_prompt: JSON.stringify(params),
          day_of_week: day.day_of_week,
          exercises: {
            create: day.exercises.map((ex, i) => ({
              name: ex.name,
              sets: ex.sets || 3,
              reps: ex.reps || '8-12',
              rest_seconds: ex.rest_seconds || 90,
              order_index: i,
              exercise_dataset_id: ex.exercise_id || null,
            })),
          },
        },
        include: { exercises: { orderBy: { order_index: 'asc' } } },
      });

      // Attach gif_url and image to each exercise for the response
      const exercisesWithGifs = routine.exercises.map((ex) => {
        let gifUrl = null;
        let image = null;
        if (ex.exercise_dataset_id) {
          const dsEx = getExerciseById(ex.exercise_dataset_id);
          if (dsEx) {
            gifUrl = dsEx.gif_url;
            image = dsEx.image;
          }
        }
        return { ...ex, gif_url: gifUrl, image };
      });

      createdDays.push({ ...routine, exercises: exercisesWithGifs });
    }

    res.status(201).json({
      plan_name: result.plan_name,
      days: createdDays,
      ai_notes: result.ai_notes,
      ai_powered: hasAI(),
    });
  } catch (err) {
    console.error('AI generate weekly error:', err);
    res.status(500).json({ error: 'Failed to generate weekly plan' });
  }
});

// Generate a single routine (legacy)
router.post('/generate-routine', async (req, res) => {
  try {
    const { focus, days_per_week, equipment, notes } = req.body;
    const user = await prisma.user.findUnique({ where: { id: req.userId } });

    const profileError = validateProfileComplete(user);
    if (profileError) return res.status(400).json({ error: profileError });

    const params = {
      age: user.age,
      weight_kg: user.weight_kg,
      height_cm: user.height_cm,
      goal: user.goal,
      focus: focus || 'full body',
      days_per_week: days_per_week || 4,
      equipment: equipment || 'all',
      notes,
    };

    const result = await generateRoutine(params);

    const routine = await prisma.routine.create({
      data: {
        user_id: req.userId,
        name: result.routine_name,
        ai_generated: true,
        ai_prompt: JSON.stringify(params),
        exercises: {
          create: result.exercises.map((ex, i) => ({
            name: ex.name,
            sets: ex.sets || 3,
            reps: ex.reps || '8-12',
            rest_seconds: ex.rest_seconds || 90,
            order_index: i,
            exercise_dataset_id: ex.exercise_id || null,
          })),
        },
      },
      include: { exercises: { orderBy: { order_index: 'asc' } } },
    });

    const exercisesWithGifs = routine.exercises.map((ex) => {
      let gifUrl = null;
      let image = null;
      if (ex.exercise_dataset_id) {
        const dsEx = getExerciseById(ex.exercise_dataset_id);
        if (dsEx) {
          gifUrl = dsEx.gif_url;
          image = dsEx.image;
        }
      }
      return { ...ex, gif_url: gifUrl, image };
    });

    res.status(201).json({ routine: { ...routine, exercises: exercisesWithGifs }, ai_notes: result.ai_notes, ai_powered: hasAI() });
  } catch (err) {
    console.error('AI generate routine error:', err);
    res.status(500).json({ error: 'Failed to generate routine' });
  }
});

// Nutrition plan with meal recommendations
router.post('/nutrition-plan', async (req, res) => {
  try {
    const user = await prisma.user.findUnique({ where: { id: req.userId } });

    const profileError = validateProfileComplete(user);
    if (profileError) return res.status(400).json({ error: profileError });

    const plan = await generateNutritionPlan({
      weight_kg: user.weight_kg,
      goal: user.goal,
      age: user.age,
      height_cm: user.height_cm,
      body_type: user.body_type,
    });

    res.json({ plan, ai_powered: hasAI() });
  } catch (err) {
    console.error('AI nutrition plan error:', err);
    res.status(500).json({ error: 'Failed to generate nutrition plan' });
  }
});

// Get routines with GIF data
router.get('/routines-with-gifs', async (req, res) => {
  try {
    const routines = await prisma.routine.findMany({
      where: { user_id: req.userId },
      include: { exercises: { orderBy: { order_index: 'asc' } } },
      orderBy: { day_of_week: 'asc' },
    });

    const routinesWithGifs = routines.map((r) => ({
      ...r,
      exercises: r.exercises.map((ex) => {
        let gifUrl = null;
        let image = null;
        if (ex.exercise_dataset_id) {
          const dsEx = getExerciseById(ex.exercise_dataset_id);
          if (dsEx) {
            gifUrl = dsEx.gif_url;
            image = dsEx.image;
          }
        }
        return { ...ex, gif_url: gifUrl, image };
      }),
    }));

    res.json({ routines: routinesWithGifs });
  } catch (err) {
    console.error('Get routines with gifs error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.get('/status', (req, res) => {
  res.json({ ai_powered: hasAI() });
});

// Analyze food photo with AI Vision
router.post('/analyze-food', async (req, res) => {
  try {
    const { image } = req.body;
    if (!image) return res.status(400).json({ error: 'Se requiere una imagen' });

    const base64 = image.replace(/^data:image\/\w+;base64,/, '');
    const result = await analyzeFoodPhoto(base64);
    res.json({ analysis: result, ai_powered: hasAI() });
  } catch (err) {
    console.error('Analyze food error:', err);
    res.status(500).json({ error: 'Error al analizar la imagen' });
  }
});

// Analyze supplement photo with AI Vision
router.post('/analyze-supplement', async (req, res) => {
  try {
    const { image } = req.body;
    if (!image) return res.status(400).json({ error: 'Se requiere una imagen' });

    const base64 = image.replace(/^data:image\/\w+;base64,/, '');
    const result = await analyzeSupplementPhoto(base64);
    res.json({ analysis: result, ai_powered: hasAI() });
  } catch (err) {
    console.error('Analyze supplement error:', err);
    res.status(500).json({ error: 'Error al analizar la imagen' });
  }
});

// Recommend recipes based on user goal and meal type
router.post('/recommend-recipes', async (req, res) => {
  try {
    const { meal_type, max_calories } = req.body;
    const user = await prisma.user.findUnique({ where: { id: req.userId } });

    const goal = user.goal || 'MAINTENANCE';
    const where = {};

    if (meal_type) where.meal_type = meal_type;

    // Filter by goal
    if (goal === 'CUTTING') {
      where.calories = { lte: max_calories || 400 };
      where.protein_g = { gte: 25 };
    } else if (goal === 'BULKING') {
      where.calories = { gte: 400 };
    }

    let recipes = await prisma.recipe.findMany({
      where,
      orderBy: { protein_g: 'desc' },
      take: 10,
    });

    // If too few results, relax filters
    if (recipes.length < 5) {
      recipes = await prisma.recipe.findMany({
        orderBy: { protein_g: 'desc' },
        take: 10,
      });
    }

    // Score recipes by goal alignment
    const scored = recipes.map((r) => {
      let score = 0;
      if (goal === 'CUTTING') {
        score += r.protein_g * 2;
        score -= r.calories * 0.1;
        score -= r.fats_g * 0.5;
      } else if (goal === 'BULKING') {
        score += r.calories * 0.1;
        score += r.protein_g;
        score += r.carbs_g * 0.5;
      } else {
        score += r.protein_g;
      }
      if (r.diet_tags && r.diet_tags.includes('high-protein')) score += 10;
      return { ...r, score };
    }).sort((a, b) => b.score - a.score);

    res.json({
      recipes: scored.slice(0, 8),
      goal,
      ai_powered: hasAI(),
    });
  } catch (err) {
    console.error('AI recipe recommendation error:', err);
    res.status(500).json({ error: 'Failed to recommend recipes' });
  }
});

module.exports = router;
