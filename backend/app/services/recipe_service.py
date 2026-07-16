from app.models.recipe_model import Recipe
from app.schemas.recipe_schema import RecipeCreate, RecipeUpdate
from app.services.base_service import CRUDBase


class RecipeService(CRUDBase[Recipe, RecipeCreate, RecipeUpdate]):
    """Capa de servicio para recetas."""


recipe_service = RecipeService(Recipe)
