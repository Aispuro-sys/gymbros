const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const recipes = [
  {
    name: 'Pollo a la Plancha con Arroz',
    description: 'Clásico de dieta fitness: pechuga de pollo magra con arroz blanco.',
    calories: 450, protein_g: 45, carbs_g: 50, fats_g: 8,
    prep_time_min: 25, servings: 1,
    ingredients: ['200g pechuga de pollo', '1 taza arroz blanco', '1 cda aceite de oliva', 'Sal, pimienta, ajo en polvo'],
    instructions: ['Cocinar el arroz según instrucciones del paquete.', 'Sazonar el pollo con sal, pimienta y ajo.', 'Calicar el aceite en una sartén y cocinar el pollo 5 min por lado.', 'Servir juntos.'],
    meal_type: 'LUNCH', diet_tags: ['high-protein', 'low-fat'],
  },
  {
    name: 'Avena con Proteína y Plátano',
    description: 'Desayuno alto en proteína con avena, whey protein y plátano.',
    calories: 380, protein_g: 30, carbs_g: 55, fats_g: 6,
    prep_time_min: 10, servings: 1,
    ingredients: ['50g avena', '1 scoop proteína de vainilla', '1 plátano', '200ml leche de almendras', 'Canela al gusto'],
    instructions: ['Cocinar la avena con la leche de almendras.', 'Mezclar la proteína con un poco de agua.', 'Combinar la avena con la proteína.', 'Topping con plátano en rodajas y canela.'],
    meal_type: 'BREAKFAST', diet_tags: ['high-protein', 'pre-workout'],
  },
  {
    name: 'Ensalada de Atún con Huevo',
    description: 'Ensalada rápida y alta en proteína.',
    calories: 320, protein_g: 35, carbs_g: 12, fats_g: 14,
    prep_time_min: 10, servings: 1,
    ingredients: ['1 lata atún en agua', '2 huevos cocidos', 'Lechuga, tomate, cebolla', '1 cda aceite de oliva', 'Limón y sal'],
    instructions: ['Cocer los huevos 10 min.', 'Mezclar el atún escurrido con los huevos picados.', 'Agregar vegetales picados.', 'Aliñar con aceite, limón y sal.'],
    meal_type: 'LUNCH', diet_tags: ['high-protein', 'low-carb', 'keto'],
  },
  {
    name: 'Batido Post-Entreno',
    description: 'Batido de recuperación con proteína, avena y fruta.',
    calories: 290, protein_g: 28, carbs_g: 38, fats_g: 4,
    prep_time_min: 5, servings: 1,
    ingredients: ['1 scoop proteína de chocolate', '1/2 taza avena', '1/2 plátano', '200ml leche', '1 cda crema de cacahuate'],
    instructions: ['Licuar todo junto hasta que quede suave.', 'Servir frío.'],
    meal_type: 'POST_WORKOUT', diet_tags: ['high-protein', 'post-workout'],
  },
  {
    name: 'Salmón al Horno con Batata',
    description: 'Salmón rico en omega-3 con batata asada.',
    calories: 520, protein_g: 38, carbs_g: 45, fats_g: 18,
    prep_time_min: 35, servings: 1,
    ingredients: ['180g filete de salmón', '1 batata mediana', '1 cda aceite de oliva', 'Eneldo, sal, limón'],
    instructions: ['Precalentar horno a 200°C.', 'Cortar la batata en cubos y hornear 20 min.', 'Sazonar el salmón con eneldo y limón.', 'Hornear el salmón 12-15 min.', 'Servir juntos.'],
    meal_type: 'DINNER', diet_tags: ['high-protein', 'omega-3'],
  },
  {
    name: 'Yogur Griego con Frutos Rojos',
    description: 'Snack alto en proteína con yogur griego y frutos rojos.',
    calories: 180, protein_g: 18, carbs_g: 22, fats_g: 3,
    prep_time_min: 5, servings: 1,
    ingredients: ['200g yogur griego natural', '1/2 taza frutos rojos mixtos', '1 cda miel', '1 cda semillas de chía'],
    instructions: ['Mezclar el yogur con la miel.', 'Agregar los frutos rojos.', 'Espolvorear chía.'],
    meal_type: 'SNACK', diet_tags: ['high-protein', 'low-fat'],
  },
  {
    name: 'Wrap de Pavo y Aguacate',
    description: 'Wrap rápido con pechuga de pavo, aguacate y vegetales.',
    calories: 340, protein_g: 28, carbs_g: 35, fats_g: 12,
    prep_time_min: 10, servings: 1,
    ingredients: ['1 tortilla integral', '100g pechuga de pavo', '1/4 aguacate', 'Lechuga, tomate', 'Mostaza'],
    instructions: ['Untar el aguacate en la tortilla.', 'Agregar pavo, lechuga y tomate.', 'Enrollar y cortar por la mitad.'],
    meal_type: 'LUNCH', diet_tags: ['high-protein', 'quick'],
  },
  {
    name: 'Tofu Salteado con Vegetales',
    description: 'Salteado vegetariano alto en proteína con tofu firme.',
    calories: 280, protein_g: 22, carbs_g: 25, fats_g: 10,
    prep_time_min: 20, servings: 1,
    ingredients: ['200g tofu firme', 'Brócoli, pimiento, zanahoria', '2 cda salsa de soya', '1 cda aceite de sésamo', 'Ajo y jengibre'],
    instructions: ['Pressar y cortar el tofu en cubos.', 'Saltear el tofu en aceite hasta dorar.', 'Agregar vegetales y saltear 5 min.', 'Sazonar con salsa de soya, ajo y jengibre.'],
    meal_type: 'DINNER', diet_tags: ['vegetarian', 'high-protein', 'vegan'],
  },
  {
    name: 'Huevos Revueltos con Espinaca',
    description: 'Desayuno bajo en carbohidratos con huevo y espinaca.',
    calories: 240, protein_g: 20, carbs_g: 6, fats_g: 15,
    prep_time_min: 10, servings: 1,
    ingredients: ['3 huevos', '1 puñado espinaca fresca', '1 cda aceite de oliva', 'Sal y pimienta'],
    instructions: ['Calentar el aceite en una sartén.', 'Saltear la espinaca 1 min.', 'Agregar los huevos batidos y revolver.', 'Sazonar y servir.'],
    meal_type: 'BREAKFAST', diet_tags: ['low-carb', 'keto', 'high-protein'],
  },
  {
    name: 'Fajitas de Pollo',
    description: 'Fajitas fitness con pollo, pimientos y especias.',
    calories: 400, protein_g: 40, carbs_g: 30, fats_g: 12,
    prep_time_min: 25, servings: 1,
    ingredients: ['200g pechuga de pollo en tiras', '1 pimiento rojo', '1 pimiento verde', '1/2 cebolla', 'Tortillas integrales', 'Comino, pimentón, sal'],
    instructions: ['Saltear el pollo hasta dorar.', 'Agregar pimientos y cebolla en tiras.', 'Sazonar con comino y pimentón.', 'Servir en tortillas calientes.'],
    meal_type: 'DINNER', diet_tags: ['high-protein', 'low-fat'],
  },
  {
    name: 'Bowl de Quinoa con Pollo',
    description: 'Bowl completo con quinoa, pollo, aguacate y vegetales.',
    calories: 480, protein_g: 38, carbs_g: 52, fats_g: 14,
    prep_time_min: 30, servings: 1,
    ingredients: ['1/2 taza quinoa', '150g pollo cocido', '1/4 aguacate', 'Tomate, pepino, maíz', 'Limón, sal, cilantro'],
    instructions: ['Cocinar la quinoa 15 min.', 'Preparar el pollo a la plancha.', 'Combinar todo en un bowl.', 'Aliñar con limón, sal y cilantro.'],
    meal_type: 'LUNCH', diet_tags: ['high-protein', 'gluten-free'],
  },
  {
    name: 'Cena Ligera de Merluza',
    description: 'Filete de merluza al vapor con vegetales.',
    calories: 260, protein_g: 32, carbs_g: 15, fats_g: 6,
    prep_time_min: 20, servings: 1,
    ingredients: ['200g filete de merluza', 'Calabacín, zanahoria, brócoli', '1 cda aceite de oliva', 'Limón, sal, eneldo'],
    instructions: ['Cocinar la merluza al vapor 10 min.', 'Saltear los vegetales 5 min.', 'Servir con aceite de oliva y limón.'],
    meal_type: 'DINNER', diet_tags: ['low-calorie', 'high-protein', 'low-fat'],
  },
];

async function main() {
  console.log('Seeding recipes...');
  for (const r of recipes) {
    await prisma.recipe.upsert({
      where: { id: r.name },
      create: { ...r, id: undefined },
      update: {},
    });
  }
  console.log(`Seeded ${recipes.length} recipes.`);
}

main()
  .catch((e) => { console.error(e); process.exit(1); })
  .finally(async () => { await prisma.$disconnect(); });
