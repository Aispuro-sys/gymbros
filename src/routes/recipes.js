const express = require('express');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

router.use(authMiddleware);

// Get recipes with filters
router.get('/', async (req, res) => {
  try {
    const { meal_type, min_protein, max_calories, search, limit } = req.query;
    const where = {};

    if (meal_type && meal_type !== 'ANY') {
      where.meal_type = meal_type;
    }
    if (min_protein) {
      where.protein_g = { gte: parseInt(min_protein) };
    }
    if (max_calories) {
      where.calories = { lte: parseInt(max_calories) };
    }
    if (search) {
      where.name = { contains: search, mode: 'insensitive' };
    }

    const recipes = await prisma.recipe.findMany({
      where,
      orderBy: { protein_g: 'desc' },
    });

    const recipesWithHttps = recipes.map((r) => ({
      ...r,
      image_url: r.image_url ? r.image_url.replace(/^http:/, 'https:') : null,
    }));

    res.json({ recipes: recipesWithHttps });
  } catch (err) {
    console.error('Recipes fetch error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get single recipe
router.get('/:id', async (req, res) => {
  try {
    const recipe = await prisma.recipe.findUnique({ where: { id: req.params.id } });
    if (!recipe) return res.status(404).json({ error: 'Recipe not found' });
    res.json({ recipe });
  } catch (err) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Create recipe (community)
router.post('/', async (req, res) => {
  try {
    const { name, description, image_url, calories, protein_g, carbs_g, fats_g, prep_time_min, servings, ingredients, instructions, meal_type, diet_tags } = req.body;
    if (!name) return res.status(400).json({ error: 'Recipe name required' });

    const recipe = await prisma.recipe.create({
      data: {
        name,
        description: description || null,
        image_url: image_url || null,
        calories: calories || 0,
        protein_g: protein_g || 0,
        carbs_g: carbs_g || 0,
        fats_g: fats_g || 0,
        prep_time_min: prep_time_min || 0,
        servings: servings || 1,
        ingredients: ingredients || [],
        instructions: instructions || [],
        meal_type: meal_type || 'ANY',
        diet_tags: diet_tags || [],
        source: 'community',
      },
    });
    res.status(201).json({ recipe });
  } catch (err) {
    console.error('Create recipe error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Generate shopping list from multiple recipes
router.post('/shopping-list', async (req, res) => {
  try {
    const { recipeIds } = req.body;
    if (!recipeIds || !Array.isArray(recipeIds) || recipeIds.length === 0) {
      return res.status(400).json({ error: 'recipeIds array is required' });
    }

    const recipes = await prisma.recipe.findMany({
      where: { id: { in: recipeIds } },
    });

    const ingredientMap = {};
    recipes.forEach((r) => {
      (r.ingredients || []).forEach((ing) => {
        const key = ing.trim().toLowerCase();
        if (!ingredientMap[key]) {
          ingredientMap[key] = { name: ing.trim(), count: 1, recipes: [r.name] };
        } else {
          ingredientMap[key].count++;
          if (!ingredientMap[key].recipes.includes(r.name)) {
            ingredientMap[key].recipes.push(r.name);
          }
        }
      });
    });

    const ingredients = Object.values(ingredientMap).sort((a, b) => a.name.localeCompare(b.name));

    res.json({
      ingredients,
      recipeCount: recipes.length,
      recipeNames: recipes.map((r) => r.name),
    });
  } catch (err) {
    console.error('Shopping list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
