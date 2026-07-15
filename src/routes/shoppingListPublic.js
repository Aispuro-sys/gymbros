const express = require('express');
const prisma = require('../lib/prisma');

const router = express.Router();

// Public route: get shared shopping list by token
router.get('/shared/:token', async (req, res) => {
  try {
    const list = await prisma.shoppingList.findFirst({
      where: { share_token: req.params.token },
      include: {
        items: { orderBy: { order_index: 'asc' } },
        user: { select: { username: true } },
      },
    });

    if (!list) {
      return res.status(404).json({ error: 'Lista no encontrada o no compartida' });
    }

    res.json({
      list: {
        id: list.id,
        name: list.name,
        created_at: list.created_at,
        items: list.items.map((item) => ({
          id: item.id,
          name: item.name,
          quantity: item.quantity,
          checked: item.checked,
          recipe_names: item.recipe_names,
        })),
      },
      sharedBy: list.user.username,
    });
  } catch (err) {
    console.error('Get shared list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Public route: toggle item checked (no auth, uses list token)
router.put('/shared/:token/items/:itemId', async (req, res) => {
  try {
    const { checked } = req.body;
    const list = await prisma.shoppingList.findFirst({
      where: { share_token: req.params.token },
    });

    if (!list) {
      return res.status(404).json({ error: 'Lista no encontrada' });
    }

    const item = await prisma.shoppingListItem.update({
      where: { id: req.params.itemId },
      data: { checked },
    });
    res.json({ item });
  } catch (err) {
    console.error('Toggle shared item error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
