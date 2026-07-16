from app.api.crud_router import make_crud_router
from app.schemas.recipe_schema import RecipeCreate, RecipeRead, RecipeUpdate
from app.services.recipe_service import recipe_service

router = make_crud_router(
    prefix="/recipes",
    service=recipe_service,
    read_schema=RecipeRead,
    create_schema=RecipeCreate,
    update_schema=RecipeUpdate,
    tags=["recipes"],
)
