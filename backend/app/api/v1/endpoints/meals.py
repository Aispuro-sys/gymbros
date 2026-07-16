from app.api.crud_router import make_crud_router
from app.schemas.meal_schema import MealCreate, MealRead, MealUpdate
from app.services.meal_service import meal_service

router = make_crud_router(
    prefix="/meals",
    service=meal_service,
    read_schema=MealRead,
    create_schema=MealCreate,
    update_schema=MealUpdate,
    tags=["meals"],
)
