require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');

const authRoutes = require('./routes/auth');
const routineRoutes = require('./routes/routines');
const macrosRoutes = require('./routes/macros');
const supplementRoutes = require('./routes/supplements');
const teamRoutes = require('./routes/teams');
const exerciseRoutes = require('./routes/exercises');
const aiRoutes = require('./routes/ai');
const mealRoutes = require('./routes/meals');
const communityRoutes = require('./routes/community');
const recipeRoutes = require('./routes/recipes');
const shoppingListRoutes = require('./routes/shoppingList');
const shoppingListPublicRoutes = require('./routes/shoppingListPublic');
const progressRoutes = require('./routes/progress');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: '15mb' }));
app.use(express.static(path.join(__dirname, '..', 'public')));
app.use('/assets', express.static(path.join(__dirname, '..', 'assets')));
app.use('/exercises-dataset', express.static(path.join(__dirname, '..', 'exercises-dataset')));

app.use('/api/auth', authRoutes);
app.use('/api/routines', routineRoutes);
app.use('/api/macros', macrosRoutes);
app.use('/api/supplements', supplementRoutes);
app.use('/api/teams', teamRoutes);
app.use('/api/exercises', exerciseRoutes);
app.use('/api/ai', aiRoutes);
app.use('/api/meals', mealRoutes);
app.use('/api/community', communityRoutes);
app.use('/api/recipes', recipeRoutes);
app.use('/api/shopping-list', shoppingListRoutes);
app.use('/api/shopping-list-public', shoppingListPublicRoutes);
app.use('/api/progress', progressRoutes);

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Talos Forge running on http://localhost:${PORT}`);
});

module.exports = app;
