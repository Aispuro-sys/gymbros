from app.models.meal_model import Meal
from app.schemas.meal_schema import MealCreate, MealUpdate
from app.services.base_service import CRUDBase


class MealService(CRUDBase[Meal, MealCreate, MealUpdate]):
    """Capa de servicio para comidas."""


meal_service = MealService(Meal)
