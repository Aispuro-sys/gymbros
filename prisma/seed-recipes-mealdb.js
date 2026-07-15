/**
 * Seed fitness recipes from TheMealDB API with images
 * Fetches from Chicken, Seafood, Vegetarian, and Breakfast categories
 * Estimates macros based on ingredients
 */
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

// Fitness-friendly meal IDs from TheMealDB (high protein, healthy)
const FITNESS_MEAL_IDS = [
  // Chicken (high protein)
  '53011', // Chicken Quinoa Greek Salad
  '52934', // Chicken Basquaise
  '52831', // Chicken Karaage
  '53016', // Chick-Fil-A Sandwich
  '52806', // Tandoori chicken
  '53218', // Chicken Shawarma
  '53039', // Piri-piri chicken and slaw
  '53261', // Chicken wings with cumin, lemon & garlic
  '52993', // Honey Balsamic Chicken with Crispy Broccoli & Potatoes
  '52813', // Kentucky Fried Chicken
  // Seafood (lean protein)
  '52959', // Baked salmon with fennel & tomatoes (via seafood category)
  '52819', // Couscous with fish (seafood)
  '52823', // Fish soup (seafood)
  // Vegetarian (nutrient dense)
  '53067', // Stuffed Bell Peppers with Quinoa and Black Beans
  '52784', // Smoky Lentil Chili with Squash
  '53012', // Gigantes Plaki (Greek beans)
  '52868', // Kidney Bean Curry
  '52869', // Tahini Lentils
  '53027', // Koshari
  '52816', // Roasted Eggplant With Tahini, Pine Nuts, and Lentils
  '53091', // Falafel Pita Sandwich with Tahini Sauce
  '53266', // Falafel
  '53047', // Moroccan Carrot Soup
  '52811', // Ribollita
  '53240', // Tofu, greens & cashew stir-fry
  '52870', // Chickpea Fajitas
  '53025', // Ful Medames
  // Breakfast
  '52963', // Shakshuka
  '53222', // Vegetarian Shakshuka
  '52921', // Provençal Omelette Cake
  '52872', // Spanish Tortilla
];

// Macro estimation based on category and ingredients
function estimateMacals(meal) {
  const name = (meal.strMeal || '').toLowerCase();
  const cat = (meal.strCategory || '').toLowerCase();
  const area = (meal.strArea || '').toLowerCase();

  let calories = 400, protein = 30, carbs = 40, fats = 15;

  // Chicken: high protein
  if (cat === 'chicken' || name.includes('chicken')) {
    calories = 450; protein = 45; carbs = 35; fats = 15;
    if (name.includes('salad')) { calories = 350; protein = 40; carbs = 20; fats = 12; }
    if (name.includes('wings')) { calories = 380; protein = 35; carbs = 8; fats = 24; }
    if (name.includes('curry') || name.includes('tandoori')) { calories = 420; protein = 38; carbs = 30; fats = 18; }
    if (name.includes('shawarma') || name.includes('burger') || name.includes('sandwich')) { calories = 520; protein = 35; carbs = 45; fats = 22; }
  }

  // Seafood: lean protein
  if (cat === 'seafood' || name.includes('fish') || name.includes('salmon') || name.includes('tuna')) {
    calories = 350; protein = 38; carbs = 20; fats = 12;
    if (name.includes('soup')) { calories = 250; protein = 25; carbs = 15; fats = 8; }
  }

  // Vegetarian: moderate protein
  if (cat === 'vegetarian') {
    calories = 320; protein = 15; carbs = 45; fats = 12;
    if (name.includes('tofu')) { protein = 25; }
    if (name.includes('bean') || name.includes('lentil') || name.includes('chickpea')) { protein = 20; }
    if (name.includes('falafel')) { calories = 380; protein = 18; carbs = 40; fats = 18; }
    if (name.includes('soup')) { calories = 220; protein = 12; carbs = 30; fats = 6; }
    if (name.includes('salad')) { calories = 280; protein = 14; carbs = 25; fats = 14; }
    if (name.includes('chili')) { calories = 340; protein = 18; carbs = 40; fats = 10; }
  }

  // Breakfast
  if (name.includes('shakshuka') || name.includes('omelette') || name.includes('tortilla')) {
    calories = 300; protein = 20; carbs = 18; fats = 18;
  }

  return { calories, protein_g: protein, carbs_g: carbs, fats_g: fats };
}

function extractIngredients(meal) {
  const ingredients = [];
  for (let i = 1; i <= 20; i++) {
    const ing = meal[`strIngredient${i}`];
    const measure = meal[`strMeasure${i}`];
    if (ing && ing.trim()) {
      ingredients.push(measure && measure.trim() ? `${measure.trim()} ${ing.trim()}` : ing.trim());
    }
  }
  return ingredients;
}

function extractInstructions(meal) {
  if (!meal.strInstructions) return [];
  return meal.strInstructions
    .split(/\r\n|\n|\r/)
    .map((s) => s.trim())
    .filter((s) => s.length > 10);
}

function inferMealType(meal) {
  const name = (meal.strMeal || '').toLowerCase();
  const cat = (meal.strCategory || '').toLowerCase();
  if (cat === 'breakfast' || name.includes('omelette') || name.includes('shakshuka') || name.includes('tortilla')) return 'BREAKFAST';
  if (name.includes('soup') || name.includes('salad') || name.includes('snack') || name.includes('falafel')) return 'SNACK';
  if (name.includes('pre') || name.includes('post')) return 'POST_WORKOUT';
  if (name.includes('dinner') || name.includes('casserole') || name.includes('roast')) return 'DINNER';
  return 'LUNCH';
}

function inferDietTags(meal) {
  const tags = [];
  const cat = (meal.strCategory || '').toLowerCase();
  const name = (meal.strMeal || '').toLowerCase();
  if (cat === 'chicken') tags.push('high-protein');
  if (cat === 'seafood') tags.push('high-protein', 'omega-3');
  if (cat === 'vegetarian') tags.push('vegetarian');
  if (name.includes('tofu')) tags.push('vegan');
  if (name.includes('salad')) tags.push('low-carb');
  if (name.includes('soup')) tags.push('low-calorie');
  if (name.includes('quinoa') || name.includes('lentil') || name.includes('bean')) tags.push('high-fiber');
  if (name.includes('egg') || name.includes('omelette')) tags.push('high-protein');
  return tags;
}

async function fetchMeal(id) {
  const res = await fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${id}`);
  const data = await res.json();
  return data.meals ? data.meals[0] : null;
}

async function seedRecipes() {
  console.log('Fetching fitness recipes from TheMealDB...\n');
  let seeded = 0;

  for (const id of FITNESS_MEAL_IDS) {
    try {
      const meal = await fetchMeal(id);
      if (!meal) { console.log(`  ✗ Meal ${id} not found`); continue; }

      const existing = await prisma.recipe.findFirst({ where: { name: meal.strMeal } });
      if (existing) { console.log(`  ⊘ ${meal.strMeal} (already exists)`); continue; }

      const macros = estimateMacals(meal);
      const ingredients = extractIngredients(meal);
      const instructions = extractInstructions(meal);
      const mealType = inferMealType(meal);
      const dietTags = inferDietTags(meal);

      await prisma.recipe.create({
        data: {
          name: meal.strMeal,
          description: `${meal.strCategory} · ${meal.strArea || 'International'}`,
          image_url: meal.strMealThumb,
          calories: macros.calories,
          protein_g: macros.protein_g,
          carbs_g: macros.carbs_g,
          fats_g: macros.fats_g,
          prep_time_min: 30,
          servings: 2,
          ingredients,
          instructions,
          meal_type: mealType,
          diet_tags: dietTags,
          source: 'themealdb',
        },
      });
      console.log(`  ✓ ${meal.strMeal}`);
      seeded++;
    } catch (err) {
      console.log(`  ✗ Error with meal ${id}: ${err.message}`);
    }
  }

  console.log(`\nDone! Seeded ${seeded} new recipes from TheMealDB.`);
  await prisma.$disconnect();
}

seedRecipes().catch((e) => { console.error(e); process.exit(1); });
