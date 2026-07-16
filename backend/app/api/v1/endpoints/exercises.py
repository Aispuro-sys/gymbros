from app.api.crud_router import make_crud_router
from app.schemas.exercise_schema import ExerciseCreate, ExerciseRead, ExerciseUpdate
from app.services.exercise_service import exercise_service

router = make_crud_router(
    prefix="/exercises",
    service=exercise_service,
    read_schema=ExerciseRead,
    create_schema=ExerciseCreate,
    update_schema=ExerciseUpdate,
    tags=["exercises"],
)
