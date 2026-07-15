const path = require('path');
const fs = require('fs');

let exercisesCache = null;

const CATEGORY_ES = {
  'waist': 'Cintura',
  'upper legs': 'Piernas superiores',
  'back': 'Espalda',
  'lower legs': 'Piernas inferiores',
  'chest': 'Pecho',
  'upper arms': 'Brazos superiores',
  'cardio': 'Cardio',
  'shoulders': 'Hombros',
  'lower arms': 'Brazos inferiores',
  'neck': 'Cuello',
};

const EQUIPMENT_ES = {
  'barbell': 'Barra',
  'dumbbell': 'Mancuernas',
  'body weight': 'Peso corporal',
  'machine': 'Máquina',
  'cable': 'Cables',
  'kettlebell': 'Kettlebell',
  'bands': 'Bandas',
  'medicine ball': 'Pelota medicinal',
  'exercise ball': 'Pelota de ejercicio',
  'foam roll': 'Rodillo de espuma',
  'e-z curl bar': 'Barra Z',
  'other': 'Otro',
};

const TARGET_ES = {
  'abs': 'Abdominales',
  'quads': 'Cuádriceps',
  'glutes': 'Glúteos',
  'hamstrings': 'Isquiotibiales',
  'lats': 'Dorsales',
  'traps': 'Trapecios',
  'pectorals': 'Pectorales',
  'biceps': 'Bíceps',
  'triceps': 'Tríceps',
  'shoulders': 'Hombros',
  'forearms': 'Antebrazos',
  'calves': 'Pantorrillas',
  'abductors': 'Abductores',
  'adductors': 'Aductores',
  'lower back': 'Lumbares',
  'middle back': 'Dorsal medio',
  'neck': 'Cuello',
  'glutes': 'Glúteos',
};

const MUSCLE_GROUP_ES = {
  'hip flexors': 'Flexores de cadera',
  'lower back': 'Lumbares',
  'quadriceps': 'Cuádriceps',
  'glutes': 'Glúteos',
  'hamstrings': 'Isquiotibiales',
  'calves': 'Pantorrillas',
  'abductors': 'Abductores',
  'adductors': 'Aductores',
  'pectorals': 'Pectorales',
  'lats': 'Dorsales',
  'traps': 'Trapecios',
  'biceps': 'Bíceps',
  'triceps': 'Tríceps',
  'forearms': 'Antebrazos',
  'deltoids': 'Deltoides',
  'abs': 'Abdominales',
  'obliques': 'Oblicuos',
};

function translateExercise(ex) {
  if (!ex) return ex;
  return {
    ...ex,
    category_es: CATEGORY_ES[ex.category] || ex.category,
    equipment_es: EQUIPMENT_ES[ex.equipment] || ex.equipment,
    target_es: TARGET_ES[ex.target] || ex.target,
    muscle_group_es: MUSCLE_GROUP_ES[ex.muscle_group] || ex.muscle_group,
    secondary_muscles_es: (ex.secondary_muscles || []).map((m) => MUSCLE_GROUP_ES[m] || m),
  };
}

function loadExercises() {
  if (exercisesCache) return exercisesCache;
  const filePath = path.join(__dirname, '..', '..', 'exercises-dataset', 'data', 'exercises.json');
  const raw = fs.readFileSync(filePath, 'utf-8');
  exercisesCache = JSON.parse(raw);
  return exercisesCache;
}

function getExercises() {
  return loadExercises().map(translateExercise);
}

function getExerciseById(id) {
  const all = loadExercises();
  const ex = all.find((e) => e.id === id);
  return ex ? translateExercise(ex) : null;
}

function searchExercises({ query, category, equipment, target, limit = 50 }) {
  let results = loadExercises();

  if (query) {
    const q = query.toLowerCase();
    results = results.filter(
      (e) =>
        e.name.toLowerCase().includes(q) ||
        (CATEGORY_ES[e.category] || '').toLowerCase().includes(q) ||
        (e.muscle_group && e.muscle_group.toLowerCase().includes(q)) ||
        (MUSCLE_GROUP_ES[e.muscle_group] || '').toLowerCase().includes(q) ||
        (e.target && e.target.toLowerCase().includes(q)) ||
        (TARGET_ES[e.target] || '').toLowerCase().includes(q)
    );
  }
  if (category) {
    results = results.filter((e) => e.category === category);
  }
  if (equipment) {
    results = results.filter((e) => e.equipment === equipment);
  }
  if (target) {
    results = results.filter((e) => e.target === target);
  }

  return results.slice(0, limit).map(translateExercise);
}

function getCategories() {
  const all = loadExercises();
  const raw = [...new Set(all.map((e) => e.category))].sort();
  return raw.map((c) => ({ value: c, label: CATEGORY_ES[c] || c }));
}

function getEquipmentTypes() {
  const all = loadExercises();
  const raw = [...new Set(all.map((e) => e.equipment))].sort();
  return raw.map((e) => ({ value: e, label: EQUIPMENT_ES[e] || e }));
}

function getTargets() {
  const all = loadExercises();
  const raw = [...new Set(all.map((e) => e.target).filter(Boolean))].sort();
  return raw.map((t) => ({ value: t, label: TARGET_ES[t] || t }));
}

module.exports = {
  getExercises,
  getExerciseById,
  searchExercises,
  getCategories,
  getEquipmentTypes,
  getTargets,
  translateExercise,
  CATEGORY_ES,
  EQUIPMENT_ES,
  TARGET_ES,
};
