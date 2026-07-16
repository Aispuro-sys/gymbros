from app.api.crud_router import make_crud_router
from app.schemas.exercise_log_schema import (
    ExerciseLogCreate,
    ExerciseLogRead,
    ExerciseLogUpdate,
)
from app.services.exercise_log_service import exercise_log_service

router = make_crud_router(
    prefix="/exercise-logs",
    service=exercise_log_service,
    read_schema=ExerciseLogRead,
    create_schema=ExerciseLogCreate,
    update_schema=ExerciseLogUpdate,
    tags=["exercise-logs"],
)
