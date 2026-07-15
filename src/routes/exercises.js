const { getExercises, searchExercises, getCategories, getEquipmentTypes, getTargets } = require('../lib/exerciseDataset');

const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  const { q, category, equipment, target, limit } = req.query;
  const results = searchExercises({ query: q, category, equipment, target, limit: parseInt(limit) || 50 });
  res.json({ exercises: results, total: results.length });
});

router.get('/categories', (req, res) => {
  res.json({ categories: getCategories() });
});

router.get('/equipment', (req, res) => {
  res.json({ equipment: getEquipmentTypes() });
});

router.get('/targets', (req, res) => {
  res.json({ targets: getTargets() });
});

router.get('/:id', (req, res) => {
  const ex = getExercises().find((e) => e.id === req.params.id);
  if (!ex) return res.status(404).json({ error: 'Exercise not found' });
  res.json({ exercise: ex });
});

module.exports = router;
