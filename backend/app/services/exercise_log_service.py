from app.models.exercise_log_model import ExerciseLog
from app.schemas.exercise_log_schema import ExerciseLogCreate, ExerciseLogUpdate
from app.services.base_service import CRUDBase


class ExerciseLogService(CRUDBase[ExerciseLog, ExerciseLogCreate, ExerciseLogUpdate]):
    """Capa de servicio para logs de ejercicios."""


exercise_log_service = ExerciseLogService(ExerciseLog)
