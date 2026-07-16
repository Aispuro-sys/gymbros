from app.models.routine_model import Routine
from app.schemas.routine_schema import RoutineCreate, RoutineUpdate
from app.services.base_service import CRUDBase


class RoutineService(CRUDBase[Routine, RoutineCreate, RoutineUpdate]):
    """Capa de servicio para rutinas."""


routine_service = RoutineService(Routine)
