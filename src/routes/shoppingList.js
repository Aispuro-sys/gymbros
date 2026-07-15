const express = require('express');
const crypto = require('crypto');
const prisma = require('../lib/prisma');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

// All routes require auth except /shared/:token
router.use(authMiddleware);

// Get user's active shopping list (most recent)
router.get('/', async (req, res) => {
  try {
    const list = await prisma.shoppingList.findFirst({
      where: { user_id: req.userId },
      orderBy: { updated_at: 'desc' },
      include: { items: { orderBy: { order_index: 'asc' } } },
    });
    res.json({ list });
  } catch (err) {
    console.error('Get shopping list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Save shopping list (create or update)
router.post('/', async (req, res) => {
  try {
    const { items, name } = req.body;

    // Find or create active list
    let list = await prisma.shoppingList.findFirst({
      where: { user_id: req.userId },
      orderBy: { updated_at: 'desc' },
    });

    if (list) {
      // Delete existing items and recreate
      await prisma.shoppingListItem.deleteMany({
        where: { shopping_list_id: list.id },
      });
      list = await prisma.shoppingList.update({
        where: { id: list.id },
        data: { name: name || 'Lista de Supermercado' },
      });
    } else {
      list = await prisma.shoppingList.create({
        data: {
          user_id: req.userId,
          name: name || 'Lista de Supermercado',
        },
      });
    }

    // Create items
    if (items && items.length > 0) {
      await prisma.shoppingListItem.createMany({
        data: items.map((item, idx) => ({
          shopping_list_id: list.id,
          name: item.name,
          quantity: item.quantity || null,
          checked: item.checked || false,
          recipe_names: item.recipe_names || [],
          order_index: idx,
        })),
      });
    }

    const fullList = await prisma.shoppingList.findUnique({
      where: { id: list.id },
      include: { items: { orderBy: { order_index: 'asc' } } },
    });

    res.json({ list: fullList });
  } catch (err) {
    console.error('Save shopping list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Toggle item checked status
router.put('/:listId/items/:itemId', async (req, res) => {
  try {
    const { checked } = req.body;
    const list = await prisma.shoppingList.findUnique({
      where: { id: req.params.listId },
    });
    if (!list || list.user_id !== req.userId) {
      return res.status(404).json({ error: 'List not found' });
    }

    const item = await prisma.shoppingListItem.update({
      where: { id: req.params.itemId },
      data: { checked: checked },
    });
    res.json({ item });
  } catch (err) {
    console.error('Toggle item error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Generate share token
router.post('/:listId/share', async (req, res) => {
  try {
    const list = await prisma.shoppingList.findUnique({
      where: { id: req.params.listId },
    });
    if (!list || list.user_id !== req.userId) {
      return res.status(404).json({ error: 'List not found' });
    }

    if (!list.share_token) {
      const token = crypto.randomBytes(16).toString('hex');
      const updated = await prisma.shoppingList.update({
        where: { id: list.id },
        data: { share_token: token },
      });
      return res.json({ shareToken: token, shareUrl: `/lista-compartida.html?token=${token}` });
    }

    res.json({ shareToken: list.share_token, shareUrl: `/lista-compartida.html?token=${list.share_token}` });
  } catch (err) {
    console.error('Share list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete shopping list
router.delete('/:listId', async (req, res) => {
  try {
    const list = await prisma.shoppingList.findUnique({
      where: { id: req.params.listId },
    });
    if (!list || list.user_id !== req.userId) {
      return res.status(404).json({ error: 'List not found' });
    }
    await prisma.shoppingList.delete({ where: { id: list.id } });
    res.json({ message: 'List deleted' });
  } catch (err) {
    console.error('Delete list error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
