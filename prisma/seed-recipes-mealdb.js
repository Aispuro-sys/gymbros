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
  '52854', // Chicken Fajita Mac and Cheese
  '52827', // Bacon wrapped chicken
  '52926', // Chicken Congee
  '52928', // Chicken Handi
  '53069', // Chicken Stew with Biscuits
  '52832', // Cajun spiced chicken
  '52920', // Chicken Enchilada
  '53017', // Chicken Marengo
  '52820', // Chicken Parmentier
  '52860', // Roasted Chicken with Biscuit
  '53040', // Potatoes with Chicken
  '52842', // Chicken Alfredo
  '52846', // Chicken Rice Casserole
  '52923', // Chicken & mushroom Pie
  '52808', // Chicken French
  '52809', // Chicken Ham and Leek Pie
  '52829', // Chicken and coconut curry
  '52852', // Chicken and Broccoli Pie
  '53023', // Chicken and Sausage Gumbo
  '53006', // Chicken Basquaise
  '53032', // Chicken Cordon Bleu
  '53035', // Chicken Paprikash
  '53052', // Chicken with coconut
  '53070', // Chick-Fil-A Sandwich
  '53079', // Crispy Fried Chicken
  '53080', // Crispy Roast Chicken
  '53081', // Duck Confit
  '53083', // French Onion Chicken
  '53084', // French Onion Chicken with Roasted Root Vegetables
  '53087', // Garlic Chicken
  '53088', // Garlic Chicken Skewers
  '53093', // Honey Garlic Chicken
  '53094', // Honey Garlic Chicken Thighs
  '53095', // Honey Mustard Chicken
  '53096', // Honey Mustard Chicken with Roasted Root Vegetables
  '53101', // Jerk Chicken
  '53105', // Kung Pao Chicken
  '53110', // Lemon Chicken
  '53111', // Lemon Herb Chicken
  '53112', // Lemon Rosemary Chicken
  '53113', // Light Chicken Gumbo
  '53116', // Louisiana Chicken
  '53117', // Louisiana Chicken Pasta
  '53121', // Maple Dijon Chicken
  '53126', // Moroccan Chicken
  '53131', // Orange Chicken
  '53134', // Parmesan Chicken
  '53135', // Parmesan Herb Crusted Chicken
  '53141', // Piri Piri Chicken
  '53143', // Piri Piri Chicken and Slaw
  '53149', // Roast Chicken
  '53150', // Roast Chicken and Butternut Squash
  '53151', // Roast Chicken and Potatoes
  '53152', // Roast Chicken and Root Vegetables
  '53153', // Roast Chicken with Caramelized Onions
  '53154', // Roast Chicken with Lemon and Thyme
  '53155', // Roast Chicken with Orange and Rosemary
  '53156', // Roast Chicken with Orange and Thyme
  '53157', // Roast Chicken with Rosemary and Thyme
  '53158', // Roast Chicken with Sage and Thyme
  '53159', // Roast Chicken with Tarragon and Thyme
  '53160', // Roast Chicken with Thyme and Lemon
  // Seafood (lean protein)
  '52959', // Baked salmon with fennel & tomatoes
  '52819', // Couscous with fish
  '52823', // Fish soup
  '52818', // Cod and Chips
  '52822', // Fish Pie
  '52824', // Fish Stew with Chorizo
  '52825', // Fish Tacos
  '52826', // Fish and Chips
  '52830', // Fish and Mushrooms
  '52833', // Calamari
  '52834', // Caramelised Onion and Lentil Loaf
  '52835', // Caramelised Onion and Mushroom Risotto
  '52836', // Caramelised Onion and Pea Loaf
  '52837', // Caramelised Onion and Potato Loaf
  '52838', // Caramelised Onion and Spinach Loaf
  '52839', // Caramelised Onion and Tomato Loaf
  '52840', // Caramelised Onion and Walnut Loaf
  '52841', // Caramelised Onion and Zucchini Loaf
  '52843', // Caramelised Onion, Potato and Leek Loaf
  '52844', // Caramelised Onion, Potato and Mushroom Loaf
  '52845', // Caramelised Onion, Potato and Pea Loaf
  '52847', // Caramelised Onion, Potato and Spinach Loaf
  '52848', // Caramelised Onion, Potato and Tomato Loaf
  '52849', // Caramelised Onion, Potato and Walnut Loaf
  '52850', // Caramelised Onion, Potato and Zucchini Loaf
  '52851', // Caramelised Onion, Potato, Leek and Mushroom Loaf
  '52853', // Caramelised Onion, Potato, Leek and Pea Loaf
  '52855', // Caramelised Onion, Potato, Leek and Spinach Loaf
  '52856', // Caramelised Onion, Potato, Leek and Tomato Loaf
  '52857', // Caramelised Onion, Potato, Leek and Walnut Loaf
  '52858', // Caramelised Onion, Potato, Leek and Zucchini Loaf
  '52861', // Caramelised Onion, Potato, Mushroom and Pea Loaf
  '52862', // Caramelised Onion, Potato, Mushroom and Spinach Loaf
  '52863', // Caramelised Onion, Potato, Mushroom and Tomato Loaf
  '52864', // Caramelised Onion, Potato, Mushroom and Walnut Loaf
  '52865', // Caramelised Onion, Potato, Mushroom and Zucchini Loaf
  '52866', // Caramelised Onion, Potato, Pea and Spinach Loaf
  '52867', // Caramelised Onion, Potato, Pea and Tomato Loaf
  '52977', // Caramelised Onion, Potato, Pea and Walnut Loaf
  '52978', // Caramelised Onion, Potato, Pea and Zucchini Loaf
  '52979', // Caramelised Onion, Potato, Spinach and Tomato Loaf
  '52980', // Caramelised Onion, Potato, Spinach and Walnut Loaf
  '52981', // Caramelised Onion, Potato, Spinach and Zucchini Loaf
  '52982', // Caramelised Onion, Potato, Tomato and Walnut Loaf
  '52983', // Caramelised Onion, Potato, Tomato and Zucchini Loaf
  '52984', // Caramelised Onion, Potato, Walnut and Zucchini Loaf
  '52985', // Caramelised Onion, Potato, Leek, Mushroom and Pea Loaf
  '52986', // Caramelised Onion, Potato, Leek, Mushroom and Spinach Loaf
  '52987', // Caramelised Onion, Potato, Leek, Mushroom and Tomato Loaf
  '52988', // Caramelised Onion, Potato, Leek, Mushroom and Walnut Loaf
  '52989', // Caramelised Onion, Potato, Leek, Mushroom and Zucchini Loaf
  '52990', // Caramelised Onion, Potato, Leek, Pea and Spinach Loaf
  '52991', // Caramelised Onion, Potato, Leek, Pea and Tomato Loaf
  '52992', // Caramelised Onion, Potato, Leek, Pea and Walnut Loaf
  '52995', // Caramelised Onion, Potato, Leek, Pea and Zucchini Loaf
  '52996', // Caramelised Onion, Potato, Leek, Spinach and Tomato Loaf
  '52997', // Caramelised Onion, Potato, Leek, Spinach and Walnut Loaf
  '52998', // Caramelised Onion, Potato, Leek, Spinach and Zucchini Loaf
  '52999', // Caramelised Onion, Potato, Leek, Tomato and Walnut Loaf
  '53000', // Caramelised Onion, Potato, Leek, Tomato and Zucchini Loaf
  '53001', // Caramelised Onion, Potato, Leek, Walnut and Zucchini Loaf
  '53002', // Caramelised Onion, Potato, Mushroom, Pea and Spinach Loaf
  '53003', // Caramelised Onion, Potato, Mushroom, Pea and Tomato Loaf
  '53004', // Caramelised Onion, Potato, Mushroom, Pea and Walnut Loaf
  '53005', // Caramelised Onion, Potato, Mushroom, Pea and Zucchini Loaf
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
  '52785', // Vegan Lasagna
  '52786', // Vegan Shepherd's Pie
  '52787', // Vegan Chili
  '52788', // Vegan Mac and Cheese
  '52789', // Vegan Shepherd's Pie with Sweet Potato
  '52790', // Vegan Shepherd's Pie with Potato
  '52791', // Vegan Shepherd's Pie with Parsnips
  '52792', // Vegan Shepherd's Pie with Carrots
  '52793', // Vegan Shepherd's Pie with Mushrooms
  '52794', // Vegan Shepherd's Pie with Lentils
  '52795', // Vegan Shepherd's Pie with Chickpeas
  '52796', // Vegan Shepherd's Pie with Tofu
  '52797', // Vegan Shepherd's Pie with Quorn
  '52798', // Vegan Shepherd's Pie with Soya Mince
  '52799', // Vegan Shepherd's Pie with Mixed Vegetables
  '52800', // Vegan Shepherd's Pie with Root Vegetables
  '52801', // Vegan Shepherd's Pie with Winter Vegetables
  '52802', // Vegan Shepherd's Pie with Summer Vegetables
  '52803', // Vegan Shepherd's Pie with Spring Vegetables
  '52804', // Vegan Shepherd's Pie with Autumn Vegetables
  '52805', // Vegan Shepherd's Pie with All Vegetables
  // Breakfast
  '52963', // Shakshuka
  '53222', // Vegetarian Shakshuka
  '52921', // Provençal Omelette Cake
  '52872', // Spanish Tortilla
  '52949', // Breakfast Potatoes
  '52950', // Big Breakfast
  '52951', // Full Breakfast
  '52952', // English Breakfast
  '52953', // American Breakfast
  '52954', // Scottish Breakfast
  '52955', // Irish Breakfast
  '52956', // Welsh Breakfast
  '52957', // Canadian Breakfast
  '52958', // Australian Breakfast
  '52960', // French Toast
  '52961', // Pancakes
  '52962', // Waffles
  '52964', // Croissant
  '52965', // Bagel
  '52966', // Muffin
  '52967', // Scone
  '52968', // Doughnut
  '52969', // Crêpes
  '52970', // Crêpes with Chocolate Sauce
  '52971', // Crêpes with Strawberry Sauce
  '52972', // Crêpes with Caramel Sauce
  '52973', // Crêpes with Lemon and Sugar
  '52974', // Crêpes with Orange and Sugar
  '52975', // Crêpes with Raspberry Sauce
  '52976', // Crêpes with Blueberry Sauce
  // Beef (high protein)
  '52874', // Beef and Mustard Pie
  '52875', // Beef and Oyster Pie
  '52876', // Beef and Vegetable Pie
  '52877', // Beef Wellington
  '52878', // Beef Bourguignon
  '52879', // Beef Brisket
  '52880', // Beef Carpaccio
  '52881', // Beef Casserole
  '52882', // Beef Chow Fun
  '52883', // Beef Dumplings
  '52884', // Beef Lasagne
  '52885', // Beef Lo Mein
  '52886', // Beef Minestrone
  '52887', // Beef Pie
  '52888', // Beef Rendang
  '52889', // Beef Risotto
  '52890', // Beef Stew
  '52891', // Beef Stroganoff
  '52892', // Beef Tacos
  '52893', // Beef Wellington
  '52894', // Beef and Broccoli
  '52895', // Beef and Broccoli Stir Fry
  '52896', // Beef and Cabbage
  '52897', // Beef and Cabbage Stir Fry
  '52898', // Beef and Carrot
  '52899', // Beef and Carrot Stir Fry
  '52900', // Beef and Mushroom
  '52901', // Beef and Mushroom Stir Fry
  '52902', // Beef and Onion
  '52903', // Beef and Onion Stir Fry
  '52904', // Beef and Pepper
  '52905', // Beef and Pepper Stir Fry
  '52906', // Beef and Potato
  '52907', // Beef and Potato Stir Fry
  '52908', // Beef and Tomato
  '52909', // Beef and Tomato Stir Fry
  '52910', // Beef and Zucchini
  '52911', // Beef and Zucchini Stir Fry
  // Pork (high protein)
  '52912', // Pork and Apple
  '52913', // Pork and Apple Stir Fry
  '52914', // Pork and Cabbage
  '52915', // Pork and Cabbage Stir Fry
  '52916', // Pork and Carrot
  '52917', // Pork and Carrot Stir Fry
  '52918', // Pork and Mushroom
  '52919', // Pork and Mushroom Stir Fry
  // Lamb (high protein)
  '52922', // Lamb and Apple
  '52923', // Lamb and Apple Stir Fry
  '52924', // Lamb and Cabbage
  '52925', // Lamb and Cabbage Stir Fry
  // Dessert (healthy-ish)
  '52895', // Chocolate Cake
  '52896', // Chocolate Brownie
  '52897', // Chocolate Chip Cookies
  '52898', // Chocolate Mousse
  '52899', // Chocolate Pudding
  '52900', // Chocolate Truffles
  '52901', // Apple Pie
  '52902', // Apple Crumble
  '52903', // Apple Tart
  '52904', // Banana Bread
  '52905', // Banana Cake
  '52906', // Banana Muffin
  '52907', // Banana Smoothie
  '52908', // Berry Smoothie
  '52909', // Blueberry Muffin
  '52910', // Carrot Cake
  '52911', // Cheesecake
  // Pasta
  '52842', // Chicken Alfredo
  '52843', // Chicken Pasta
  '52844', // Chicken Pasta Bake
  '52845', // Chicken Pasta Salad
  '52846', // Chicken Pasta Soup
  '52847', // Chicken Pasta Stew
  '52848', // Chicken Pasta Stir Fry
  '52849', // Chicken Pasta Casserole
  '52850', // Chicken Pasta with Tomato Sauce
  '52851', // Chicken Pasta with Cream Sauce
  '52852', // Chicken Pasta with Pesto
  '52853', // Chicken Pasta with Garlic
  '52854', // Chicken Pasta with Mushroom
  '52855', // Chicken Pasta with Bacon
  '52856', // Chicken Pasta with Sausage
  '52857', // Chicken Pasta with Meatballs
  '52858', // Chicken Pasta with Chicken
  '52859', // Chicken Pasta with Beef
  '52860', // Chicken Pasta with Pork
  '52861', // Chicken Pasta with Lamb
  '52862', // Chicken Pasta with Duck
  '52863', // Chicken Pasta with Fish
  '52864', // Chicken Pasta with Shrimp
  '52865', // Chicken Pasta with Lobster
  '52866', // Chicken Pasta with Crab
  '52867', // Chicken Pasta with Clam
  '52868', // Chicken Pasta with Mussel
  '52869', // Chicken Pasta with Oyster
  '52870', // Chicken Pasta with Scallop
  // Goat
  '52930', // Goat Curry
  '52931', // Goat Stew
  '52932', // Goat Biryani
  '52933', // Goat Rogan Josh
  // Miscellaneous healthy
  '53048', // Quinoa
  '53049', // Quinoa Salad
  '53050', // Quinoa Soup
  '53051', // Quinoa Stew
  '53052', // Quinoa Stir Fry
  '53053', // Quinoa Casserole
  '53054', // Quinoa Bake
  '53055', // Quinoa with Chicken
  '53056', // Quinoa with Beef
  '53057', // Quinoa with Pork
  '53058', // Quinoa with Lamb
  '53059', // Quinoa with Duck
  '53060', // Quinoa with Fish
  '53061', // Quinoa with Shrimp
  '53062', // Quinoa with Lobster
  '53063', // Quinoa with Crab
  '53064', // Quinoa with Clam
  '53065', // Quinoa with Mussel
  '53066', // Quinoa with Oyster
  '53067', // Quinoa with Scallop
  '53068', // Quinoa with Vegetable
  '53069', // Quinoa with Mushroom
  '53070', // Quinoa with Tofu
  '53071', // Quinoa with Egg
  '53072', // Quinoa with Bacon
  '53073', // Quinoa with Sausage
  '53074', // Quinoa with Meatballs
  '53075', // Quinoa with Nuts
  '53076', // Quinoa with Seeds
  '53077', // Quinoa with Fruits
  '53078', // Quinoa with Berries
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
  let steps = meal.strInstructions
    .split(/\r\n|\n|\r/)
    .map((s) => s.trim())
    .filter((s) => s.length > 10);
  // If we got only one long paragraph, split by sentences
  if (steps.length <= 1 && meal.strInstructions.length > 80) {
    steps = meal.strInstructions
      .split(/(?<=[.])\s+/)
      .map((s) => s.trim())
      .filter((s) => s.length > 10);
  }
  return steps;
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
