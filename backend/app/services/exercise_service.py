from app.models.exercise_model import Exercise
from app.schemas.exercise_schema import ExerciseCreate, ExerciseUpdate
from app.services.base_service import CRUDBase


class ExerciseService(CRUDBase[Exercise, ExerciseCreate, ExerciseUpdate]):
    """Capa de servicio para ejercicios."""


exercise_service = ExerciseService(Exercise)
