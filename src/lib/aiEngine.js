const { getExercises } = require('./exerciseDataset');

function hasOpenAI() {
  return !!process.env.OPENAI_API_KEY;
}

function hasGemini() {
  return !!process.env.GEMINI_API_KEY;
}

function hasAI() {
  return hasOpenAI() || hasGemini();
}

// ===== Google Gemini (free tier) =====
async function callGeminiVision(systemPrompt, base64Image, userText) {
  const model = process.env.GEMINI_MODEL || 'gemini-1.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${process.env.GEMINI_API_KEY}`;

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      system_instruction: {
        parts: [{ text: systemPrompt }],
      },
      contents: [
        {
          role: 'user',
          parts: [
            { text: userText + '\n\nResponde SOLO con JSON válido, sin texto adicional ni markdown.' },
            {
              inline_data: {
                mime_type: 'image/jpeg',
                data: base64Image,
              },
            },
          ],
        },
      ],
      generationConfig: {
        temperature: 0.3,
        maxOutputTokens: 800,
        responseMimeType: 'application/json',
      },
    }),
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Gemini API error: ${err}`);
  }

  const data = await res.json();
  const text = data.candidates?.[0]?.content?.parts?.[0]?.text || '{}';
  return JSON.parse(text);
}

async function callGeminiText(systemPrompt, userPrompt) {
  const model = process.env.GEMINI_MODEL || 'gemini-1.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${process.env.GEMINI_API_KEY}`;

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      system_instruction: {
        parts: [{ text: systemPrompt }],
      },
      contents: [
        {
          role: 'user',
          parts: [{ text: userPrompt + '\n\nResponde SOLO con JSON válido, sin texto adicional ni markdown.' }],
        },
      ],
      generationConfig: {
        temperature: 0.7,
        maxOutputTokens: 2000,
        responseMimeType: 'application/json',
      },
    }),
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Gemini API error: ${err}`);
  }

  const data = await res.json();
  const text = data.candidates?.[0]?.content?.parts?.[0]?.text || '{}';
  return JSON.parse(text);
}

// ===== Unified AI callers (tries Gemini first, then OpenAI) =====
async function callAI(systemPrompt, userPrompt) {
  if (hasGemini()) {
    try {
      return await callGeminiText(systemPrompt, userPrompt);
    } catch (err) {
      console.error('Gemini text error, falling back:', err.message);
    }
  }
  if (hasOpenAI()) {
    return await callOpenAI(systemPrompt, userPrompt);
  }
  throw new Error('No AI provider configured');
}

async function callAIVision(systemPrompt, base64Image, userText) {
  if (hasGemini()) {
    try {
      return await callGeminiVision(systemPrompt, base64Image, userText);
    } catch (err) {
      console.error('Gemini vision error, falling back to OpenAI:', err.message);
    }
  }
  if (hasOpenAI()) {
    return await callOpenAIVision(systemPrompt, base64Image, userText);
  }
  throw new Error('No AI provider configured for vision');
}

async function callOpenAI(systemPrompt, userPrompt) {
  const model = process.env.AI_MODEL || 'gpt-4o';
  const res = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
    },
    body: JSON.stringify({
      model,
      messages: [
        { role: 'system', content: systemPrompt },
        { role: 'user', content: userPrompt },
      ],
      temperature: 0.7,
      response_format: { type: 'json_object' },
    }),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(`OpenAI API error: ${err}`);
  }
  const data = await res.json();
  return JSON.parse(data.choices[0].message.content);
}

async function callOpenAIVision(systemPrompt, base64Image, userText) {
  const model = process.env.AI_MODEL || 'gpt-4o';
  const res = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
    },
    body: JSON.stringify({
      model,
      messages: [
        { role: 'system', content: systemPrompt },
        {
          role: 'user',
          content: [
            { type: 'text', text: userText },
            { type: 'image_url', image_url: { url: `data:image/jpeg;base64,${base64Image}` } },
          ],
        },
      ],
      temperature: 0.3,
      response_format: { type: 'json_object' },
      max_tokens: 800,
    }),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(`OpenAI Vision API error: ${err}`);
  }
  const data = await res.json();
  return JSON.parse(data.choices[0].message.content);
}

// ===== Weekly split definitions =====
const WEEKLY_SPLITS = {
  3: [
    { day: 1, label: 'Lunes', focus: 'full body' },
    { day: 3, label: 'Miércoles', focus: 'full body' },
    { day: 5, label: 'Viernes', focus: 'full body' },
  ],
  4: [
    { day: 1, label: 'Lunes', focus: 'upper body' },
    { day: 2, label: 'Martes', focus: 'lower body' },
    { day: 4, label: 'Jueves', focus: 'upper body' },
    { day: 5, label: 'Viernes', focus: 'lower body' },
  ],
  5: [
    { day: 1, label: 'Lunes', focus: 'push' },
    { day: 2, label: 'Martes', focus: 'pull' },
    { day: 3, label: 'Miércoles', focus: 'legs' },
    { day: 5, label: 'Viernes', focus: 'push' },
    { day: 6, label: 'Sábado', focus: 'pull' },
  ],
  6: [
    { day: 1, label: 'Lunes', focus: 'push' },
    { day: 2, label: 'Martes', focus: 'pull' },
    { day: 3, label: 'Miércoles', focus: 'legs' },
    { day: 4, label: 'Jueves', focus: 'push' },
    { day: 5, label: 'Viernes', focus: 'pull' },
    { day: 6, label: 'Sábado', focus: 'legs' },
  ],
};

const FOCUS_MAP = {
  'full body': ['chest', 'back', 'upper legs', 'shoulders', 'upper arms'],
  push: ['chest', 'shoulders', 'upper arms'],
  pull: ['back', 'upper arms'],
  legs: ['upper legs', 'lower legs', 'waist'],
  'upper body': ['chest', 'back', 'shoulders', 'upper arms'],
  'lower body': ['upper legs', 'lower legs'],
};

const FOCUS_ES = {
  'full body': 'Cuerpo Completo',
  push: 'Push (Pecho/Hombros/Tríceps)',
  pull: 'Pull (Espalda/Bíceps)',
  legs: 'Piernas',
  'upper body': 'Tren Superior',
  'lower body': 'Tren Inferior',
};

const GOAL_CONFIG = {
  BULKING: { sets: 4, reps: '6-8', rest: 120 },
  CUTTING: { sets: 3, reps: '12-15', rest: 60 },
  MAINTENANCE: { sets: 3, reps: '8-12', rest: 90 },
};

const SYSTEM_PROMPT_WEEKLY = `You are an elite fitness coach AI. You create a personalized WEEKLY workout plan based on user biometrics, goals, and available equipment.

You have access to an exercise dataset with 1,324 exercises. Each exercise has: id, name, category, equipment, target, muscle_group, image, gif_url.

You MUST respond with a JSON object in this exact format:
{
  "plan_name": "string - name for the weekly plan",
  "days": [
    {
      "day_of_week": number (1=Monday, 7=Sunday),
      "label": "string - day name in Spanish",
      "focus": "string - muscle focus",
      "exercises": [
        {
          "exercise_id": "string - ID from dataset",
          "name": "string",
          "sets": number,
          "reps": "string",
          "rest_seconds": number
        }
      ]
    }
  ],
  "ai_notes": "string - coaching notes"
}

Rules:
- Rest days have empty exercises array
- 5-7 exercises per training day
- Match exercises to the day's focus
- Consider available equipment
- Adjust sets/reps/rest based on goal and day focus`;

function buildWeeklyPrompt(params, availableExercises) {
  const { age, weight_kg, height_cm, goal, body_type, days_per_week, equipment, notes } = params;
  const goalMap = { BULKING: 'ganancia de masa muscular', CUTTING: 'definición y pérdida de grasa', MAINTENANCE: 'mantenimiento y fuerza general' };
  const bodyTypeMap = { ECTOMORPH: 'ectomorfo (delgado, metabolismo rápido, difícil ganar peso)', MESOMORPH: 'mesomorfo (atlético, responde bien al entrenamiento)', ENDOMORPH: 'endomorfo (tendencia a ganar grasa, metabolismo más lento)' };
  const split = WEEKLY_SPLITS[days_per_week] || WEEKLY_SPLITS[4];

  let bmi = 'N/A';
  if (weight_kg && height_cm) {
    bmi = (weight_kg / Math.pow(height_cm / 100, 2)).toFixed(1);
  }

  let prompt = `User profile:
- Age: ${age || 'N/A'}
- Weight: ${weight_kg || 'N/A'} kg
- Height: ${height_cm || 'N/A'} cm
- BMI: ${bmi}
- Body type: ${bodyTypeMap[body_type] || 'N/A'}
- Goal: ${goalMap[goal] || goal}
- Training days per week: ${days_per_week}
- Available equipment: ${equipment || 'all'}`;

  if (notes) prompt += `\n- Additional notes: ${notes}`;

  prompt += `\n\nWeekly split to follow:
${split.map((d) => `Day ${d.day} (${d.label}): ${d.focus}`).join('\n')}

Available exercises from dataset (id | name | category | equipment | target):
${availableExercises.map((e) => `${e.id} | ${e.name} | ${e.category} | ${e.equipment} | ${e.target}`).join('\n')}

Create a weekly plan using ONLY exercises from the list above. Respond in JSON.`;

  return prompt;
}

const BODY_TYPE_CONFIG = {
  ECTOMORPH: { extraSets: 0, extraReps: 2, lessRest: 0, volumeMultiplier: 1.1, cardioAdvice: 'Limita el cardio a 2 sesiones cortas/semana' },
  MESOMORPH: { extraSets: 0, extraReps: 0, lessRest: 0, volumeMultiplier: 1.0, cardioAdvice: 'Incluye 2-3 sesiones de cardio moderado/semana' },
  ENDOMORPH: { extraSets: 1, extraReps: 3, lessRest: 15, volumeMultiplier: 1.15, cardioAdvice: 'Añade 3-4 sesiones de cardio/semana para maximizar quema' },
};

function generateFallbackWeekly(params) {
  const { goal, equipment, days_per_week, body_type, weight_kg, height_cm } = params;
  const all = getExercises();
  const split = WEEKLY_SPLITS[days_per_week] || WEEKLY_SPLITS[4];
  const cfg = GOAL_CONFIG[goal] || GOAL_CONFIG.MAINTENANCE;
  const btCfg = BODY_TYPE_CONFIG[body_type] || BODY_TYPE_CONFIG.MESOMORPH;

  let bmi = null;
  if (weight_kg && height_cm) {
    bmi = weight_kg / Math.pow(height_cm / 100, 2);
  }

  let filtered = all;
  if (equipment && equipment !== 'all') {
    filtered = filtered.filter((e) => e.equipment === equipment);
  }

  const exerciseCount = Math.round(6 * btCfg.volumeMultiplier);

  const days = split.map((dayInfo) => {
    const targetCats = FOCUS_MAP[dayInfo.focus] || FOCUS_MAP['full body'];
    let selected = filtered.filter((e) => targetCats.includes(e.category));
    if (selected.length < 5) {
      selected = filtered.slice(0, 8);
    }
    const shuffled = [...selected].sort(() => Math.random() - 0.5).slice(0, exerciseCount);
    const pool = selected.filter((e) => !shuffled.includes(e));

    const sets = cfg.sets + btCfg.extraSets;
    const repsBase = cfg.reps.split('-');
    const reps = parseInt(repsBase[0]) + btCfg.extraReps + '-' + (parseInt(repsBase[1]) + btCfg.extraReps);
    const rest = Math.max(30, cfg.rest - btCfg.lessRest);

    return {
      day_of_week: dayInfo.day,
      label: dayInfo.label,
      focus: FOCUS_ES[dayInfo.focus] || dayInfo.focus,
      exercises: shuffled.map((ex) => {
        const alts = pool
          .filter((e) => e.category === ex.category && e.id !== ex.id)
          .slice(0, 2)
          .map((alt) => ({ exercise_id: alt.id, name: alt.name, gif_url: alt.gif_url, image: alt.image }));
        return {
          exercise_id: ex.id,
          name: ex.name,
          sets,
          reps,
          rest_seconds: rest,
          gif_url: ex.gif_url,
          image: ex.image,
          alternatives: alts,
        };
      }),
    };
  });

  const goalLabel = goal === 'BULKING' ? 'Volumen' : goal === 'CUTTING' ? 'Definición' : 'Mantenimiento';
  const bodyTypeLabel = body_type === 'ECTOMORPH' ? 'Ectomorfo' : body_type === 'ENDOMORPH' ? 'Endomorfo' : body_type === 'MESOMORPH' ? 'Mesomorfo' : '';
  const bmiText = bmi ? ` Tu IMC es ${bmi.toFixed(1)} (${bmi < 18.5 ? 'bajo peso' : bmi < 25 ? 'peso normal' : bmi < 30 ? 'sobrepeso' : 'obesidad'}).` : '';

  return {
    plan_name: `Plan Semanal - ${goalLabel}${bodyTypeLabel ? ' (' + bodyTypeLabel + ')' : ''} (${days_per_week} días)`,
    days,
    ai_notes: `Plan semanal generado para ${goalLabel.toLowerCase()}.${bodyTypeLabel ? ' Sometotipo: ' + bodyTypeLabel + '.' : ''}${bmiText} ${
      goal === 'BULKING'
        ? 'Enfócate en progresión de carga con descansos largos. Come en superávit calórico.'
        : goal === 'CUTTING'
          ? 'Mantén intensidad alta con descansos cortos. Come en déficit calórico.'
          : 'Equilibra carga y volumen. Mantén calorías en mantenimiento.'
    } ${btCfg.cardioAdvice}. Descansa los días libres. Calienta 5-10 min antes de cada sesión.`,
  };
}

async function generateWeeklyRoutine(params) {
  if (hasAI()) {
    try {
      let availableExercises = getExercises();
      if (params.equipment && params.equipment !== 'all') {
        availableExercises = availableExercises.filter((e) => e.equipment === params.equipment);
      }
      availableExercises = availableExercises.slice(0, 200);
      const userPrompt = buildWeeklyPrompt(params, availableExercises);
      const result = await callAI(SYSTEM_PROMPT_WEEKLY, userPrompt);

      const allExercises = getExercises();
      const exerciseMap = {};
      allExercises.forEach((e) => { exerciseMap[e.id] = e; });

      result.days.forEach((day) => {
        day.exercises.forEach((ex) => {
          const dsEx = exerciseMap[ex.exercise_id];
          if (dsEx) {
            ex.gif_url = dsEx.gif_url;
            ex.image = dsEx.image;
          }
        });
      });

      return result;
    } catch (err) {
      console.error('OpenAI error, falling back:', err.message);
      return generateFallbackWeekly(params);
    }
  }
  return generateFallbackWeekly(params);
}

// ===== Single routine (legacy) =====
async function generateRoutine(params) {
  const weekly = await generateWeeklyRoutine({
    ...params,
    days_per_week: 1,
  });
  const day = weekly.days[0];
  return {
    routine_name: day.label + ' - ' + day.focus,
    exercises: day.exercises,
    ai_notes: weekly.ai_notes,
  };
}

// ===== Nutrition plan + food recommendations =====
async function generateNutritionPlan(params) {
  const { weight_kg, goal, age, height_cm, body_type } = params;

  let calories, protein, carbs, fats;

  const btMultiplier = { ECTOMORPH: 1.1, MESOMORPH: 1.0, ENDOMORPH: 0.9 };
  const btMult = btMultiplier[body_type] || 1.0;

  if (goal === 'BULKING') {
    calories = Math.round(weight_kg * 33 * btMult);
    protein = Math.round(weight_kg * 2.2);
    fats = Math.round(weight_kg * 1.0);
    carbs = Math.round((calories - protein * 4 - fats * 9) / 4);
  } else if (goal === 'CUTTING') {
    calories = Math.round(weight_kg * 26 * btMult);
    protein = Math.round(weight_kg * 2.5);
    fats = Math.round(weight_kg * 0.8);
    carbs = Math.round((calories - protein * 4 - fats * 9) / 4);
  } else {
    calories = Math.round(weight_kg * 30 * btMult);
    protein = Math.round(weight_kg * 2.0);
    fats = Math.round(weight_kg * 0.9);
    carbs = Math.round((calories - protein * 4 - fats * 9) / 4);
  }

  const meals = generateMealRecommendations(goal, calories, protein, carbs, fats);

  let bmi = null;
  if (weight_kg && height_cm) {
    bmi = (weight_kg / Math.pow(height_cm / 100, 2)).toFixed(1);
  }
  const btLabel = body_type === 'ECTOMORPH' ? 'ectomorfo' : body_type === 'ENDOMORPH' ? 'endomorfo' : body_type === 'MESOMORPH' ? 'mesomorfo' : '';

  return {
    calories,
    protein_g: protein,
    carbs_g: carbs,
    fats_g: fats,
    meals,
    notes: `Plan calculado para ${goal === 'BULKING' ? 'volumen' : goal === 'CUTTING' ? 'definición' : 'mantenimiento'} basado en ${weight_kg}kg${btLabel ? ' y somatotipo ' + btLabel : ''}${bmi ? '. IMC: ' + bmi : ''}. Ajusta según tu progreso semanal.`,
  };
}

function generateMealRecommendations(goal, calories, protein, carbs, fats) {
  const mealCalories = Math.round(calories / 4);

  const mealTemplates = {
    BULKING: [
      {
        meal: 'Desayuno',
        foods: ['Avena (80g) con plátano y miel', '4 huevos enteros revueltos', '1 vaso de leche entera', '1 puñado de almendras'],
        macros: `~${Math.round(mealCalories * 1.1)} kcal · P:${Math.round(protein * 0.3)}g · C:${Math.round(carbs * 0.35)}g · G:${Math.round(fats * 0.3)}g`,
        meal_type: 'BREAKFAST',
        numeric: { calories: Math.round(mealCalories * 1.1), protein_g: Math.round(protein * 0.3), carbs_g: Math.round(carbs * 0.35), fats_g: Math.round(fats * 0.3) },
      },
      {
        meal: 'Almuerzo',
        foods: ['200g pechuga de pollo a la plancha', '200g arroz blanco', '1 aguacate', 'Ensalada mixta con aceite de oliva'],
        macros: `~${Math.round(mealCalories * 1.2)} kcal · P:${Math.round(protein * 0.35)}g · C:${Math.round(carbs * 0.3)}g · G:${Math.round(fats * 0.3)}g`,
        meal_type: 'LUNCH',
        numeric: { calories: Math.round(mealCalories * 1.2), protein_g: Math.round(protein * 0.35), carbs_g: Math.round(carbs * 0.3), fats_g: Math.round(fats * 0.3) },
      },
      {
        meal: 'Pre-entreno',
        foods: ['1 plátano grande', '1 rebanada de pan integral con mermelada', '1 café negro', '30g suero de proteína en agua'],
        macros: `~${Math.round(mealCalories * 0.8)} kcal · P:${Math.round(protein * 0.15)}g · C:${Math.round(carbs * 0.2)}g · G:${Math.round(fats * 0.1)}g`,
        meal_type: 'PRE_WORKOUT',
        numeric: { calories: Math.round(mealCalories * 0.8), protein_g: Math.round(protein * 0.15), carbs_g: Math.round(carbs * 0.2), fats_g: Math.round(fats * 0.1) },
      },
      {
        meal: 'Cena',
        foods: ['180g salmón a la plancha', '150g boniato asado', 'Brócoli al vapor', '1 cucharada de aceite de oliva'],
        macros: `~${Math.round(mealCalories * 0.9)} kcal · P:${Math.round(protein * 0.2)}g · C:${Math.round(carbs * 0.15)}g · G:${Math.round(fats * 0.3)}g`,
        meal_type: 'DINNER',
        numeric: { calories: Math.round(mealCalories * 0.9), protein_g: Math.round(protein * 0.2), carbs_g: Math.round(carbs * 0.15), fats_g: Math.round(fats * 0.3) },
      },
    ],
    CUTTING: [
      {
        meal: 'Desayuno',
        foods: ['3 huevos revueltos con espinacas', '1 rebanada de pan integral', '1 café negro', '1 manzana'],
        macros: `~${Math.round(mealCalories * 0.9)} kcal · P:${Math.round(protein * 0.3)}g · C:${Math.round(carbs * 0.2)}g · G:${Math.round(fats * 0.25)}g`,
        meal_type: 'BREAKFAST',
        numeric: { calories: Math.round(mealCalories * 0.9), protein_g: Math.round(protein * 0.3), carbs_g: Math.round(carbs * 0.2), fats_g: Math.round(fats * 0.25) },
      },
      {
        meal: 'Almuerzo',
        foods: ['180g pechuga de pavo a la plancha', '100g arroz integral', 'Ensalada grande con tomate y pepino', '1 cucharadita de aceite de oliva'],
        macros: `~${Math.round(mealCalories)} kcal · P:${Math.round(protein * 0.35)}g · C:${Math.round(carbs * 0.25)}g · G:${Math.round(fats * 0.25)}g`,
        meal_type: 'LUNCH',
        numeric: { calories: Math.round(mealCalories), protein_g: Math.round(protein * 0.35), carbs_g: Math.round(carbs * 0.25), fats_g: Math.round(fats * 0.25) },
      },
      {
        meal: 'Pre-entreno',
        foods: ['1 plátano', '1 café negro', '30g suero de proteína en agua'],
        macros: `~${Math.round(mealCalories * 0.5)} kcal · P:${Math.round(protein * 0.15)}g · C:${Math.round(carbs * 0.15)}g · G:${Math.round(fats * 0.05)}g`,
        meal_type: 'PRE_WORKOUT',
        numeric: { calories: Math.round(mealCalories * 0.5), protein_g: Math.round(protein * 0.15), carbs_g: Math.round(carbs * 0.15), fats_g: Math.round(fats * 0.05) },
      },
      {
        meal: 'Cena',
        foods: ['150g merluza al horno', '80g boniato', 'Brócoli y calabacín al vapor', '1 cucharadita de aceite de oliva'],
        macros: `~${Math.round(mealCalories * 0.8)} kcal · P:${Math.round(protein * 0.2)}g · C:${Math.round(carbs * 0.1)}g · G:${Math.round(fats * 0.2)}g`,
        meal_type: 'DINNER',
        numeric: { calories: Math.round(mealCalories * 0.8), protein_g: Math.round(protein * 0.2), carbs_g: Math.round(carbs * 0.1), fats_g: Math.round(fats * 0.2) },
      },
    ],
    MAINTENANCE: [
      {
        meal: 'Desayuno',
        foods: ['Avena (60g) con frutos rojos', '3 huevos revueltos', '1 vaso de leche desnatada'],
        macros: `~${Math.round(mealCalories)} kcal · P:${Math.round(protein * 0.3)}g · C:${Math.round(carbs * 0.3)}g · G:${Math.round(fats * 0.25)}g`,
        meal_type: 'BREAKFAST',
        numeric: { calories: Math.round(mealCalories), protein_g: Math.round(protein * 0.3), carbs_g: Math.round(carbs * 0.3), fats_g: Math.round(fats * 0.25) },
      },
      {
        meal: 'Almuerzo',
        foods: ['180g pollo a la plancha', '150g arroz', 'Ensalada mixta', '1/2 aguacate'],
        macros: `~${Math.round(mealCalories * 1.1)} kcal · P:${Math.round(protein * 0.35)}g · C:${Math.round(carbs * 0.3)}g · G:${Math.round(fats * 0.3)}g`,
        meal_type: 'LUNCH',
        numeric: { calories: Math.round(mealCalories * 1.1), protein_g: Math.round(protein * 0.35), carbs_g: Math.round(carbs * 0.3), fats_g: Math.round(fats * 0.3) },
      },
      {
        meal: 'Pre-entreno',
        foods: ['1 plátano', '1 café', '1 puñado de almendras'],
        macros: `~${Math.round(mealCalories * 0.6)} kcal · P:${Math.round(protein * 0.1)}g · C:${Math.round(carbs * 0.2)}g · G:${Math.round(fats * 0.1)}g`,
        meal_type: 'PRE_WORKOUT',
        numeric: { calories: Math.round(mealCalories * 0.6), protein_g: Math.round(protein * 0.1), carbs_g: Math.round(carbs * 0.2), fats_g: Math.round(fats * 0.1) },
      },
      {
        meal: 'Cena',
        foods: ['160g salmón o atún', '120g patata asada', 'Verduras al vapor', '1 cucharada de aceite de oliva'],
        macros: `~${Math.round(mealCalories * 0.9)} kcal · P:${Math.round(protein * 0.25)}g · C:${Math.round(carbs * 0.2)}g · G:${Math.round(fats * 0.3)}g`,
        meal_type: 'DINNER',
        numeric: { calories: Math.round(mealCalories * 0.9), protein_g: Math.round(protein * 0.25), carbs_g: Math.round(carbs * 0.2), fats_g: Math.round(fats * 0.3) },
      },
    ],
  };

  return mealTemplates[goal] || mealTemplates.MAINTENANCE;
}

// ===== Photo Analysis: Food & Supplements =====

const FOOD_VISION_PROMPT = `Eres un nutricionista experto. Analiza la foto del alimento o plato de comida y estima los valores nutricionales.

Responde en JSON con este formato:
{
  "food_name": "nombre del alimento o plato",
  "estimated_portion": "porción estimada en gramos o unidades",
  "calories": número,
  "protein_g": número,
  "carbs_g": número,
  "fats_g": número,
  "fiber_g": número o null,
  "sugar_g": número o null,
  "sodium_mg": número o null,
  "confidence": "high" | "medium" | "low",
  "notes": "observaciones sobre la estimación"
}

Estima basándote en lo que ves. Si no puedes identificar el alimento, devuelve calories: 0 y notes explicando.`;

const SUPPLEMENT_VISION_PROMPT = `Eres un experto en suplementos deportivos y nutrición. Analiza la foto del frasco/etiqueta del suplemento y extrae la información nutricional.

Responde en JSON con este formato:
{
  "name": "nombre del producto",
  "brand": "marca o null si no se ve",
  "serving_size": "tamaño de porción ej: '1 cápsula' o '30g'",
  "servings_per_container": número o null,
  "calories": número o 0,
  "protein_g": número o 0,
  "carbs_g": número o 0,
  "fats_g": número o 0,
  "key_ingredients": ["lista de ingredientes principales"],
  "dose_per_serving": "dosis por porción ej: '25g proteína, 5g creatina'",
  "category": "Proteína" | "Creatina" | "Pre-entreno" | "Vitaminas" | "Aminoácidos" | "Quemador" | "Otro",
  "usage_instructions": "instrucciones de uso si se ven en la etiqueta o null",
  "warnings": "advertencias si se ven o null",
  "confidence": "high" | "medium" | "low",
  "notes": "observaciones"
}

Extrae toda la información visible en la etiqueta. Si no puedes leer algo, usa null.`;

async function analyzeFoodPhoto(base64Image) {
  if (hasAI()) {
    try {
      return await callAIVision(
        FOOD_VISION_PROMPT,
        base64Image,
        'Analiza esta imagen de comida y estima los macros. Responde en JSON.'
      );
    } catch (err) {
      console.error('Vision food error, returning fallback:', err.message);
    }
  }
  return {
    food_name: 'No identificado',
    estimated_portion: 'N/A',
    calories: 0,
    protein_g: 0,
    carbs_g: 0,
    fats_g: 0,
    fiber_g: null,
    sugar_g: null,
    sodium_mg: null,
    confidence: 'low',
    notes: hasAI()
      ? 'Error al analizar la imagen. Intenta de nuevo.'
      : 'Se requiere una API key de IA (GEMINI_API_KEY o OPENAI_API_KEY) para analizar fotos. Configúrala en .env',
  };
}

async function analyzeSupplementPhoto(base64Image) {
  if (hasAI()) {
    try {
      return await callAIVision(
        SUPPLEMENT_VISION_PROMPT,
        base64Image,
        'Analiza esta imagen del frasco/etiqueta del suplemento y extrae la información. Responde en JSON.'
      );
    } catch (err) {
      console.error('Vision supplement error, returning fallback:', err.message);
    }
  }
  return {
    name: 'No identificado',
    brand: null,
    serving_size: 'N/A',
    servings_per_container: null,
    calories: 0,
    protein_g: 0,
    carbs_g: 0,
    fats_g: 0,
    key_ingredients: [],
    dose_per_serving: 'N/A',
    category: 'Otro',
    usage_instructions: null,
    warnings: null,
    confidence: 'low',
    notes: hasAI()
      ? 'Error al analizar la imagen. Intenta de nuevo.'
      : 'Se requiere una API key de IA (GEMINI_API_KEY o OPENAI_API_KEY) para analizar fotos. Configúrala en .env',
  };
}

module.exports = { generateRoutine, generateWeeklyRoutine, generateNutritionPlan, hasOpenAI, hasGemini, hasAI, analyzeFoodPhoto, analyzeSupplementPhoto };
