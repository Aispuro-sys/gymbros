from app.models.team_routine_model import TeamRoutine
from app.schemas.team_routine_schema import TeamRoutineCreate, TeamRoutineUpdate
from app.services.base_service import CRUDBase


class TeamRoutineService(CRUDBase[TeamRoutine, TeamRoutineCreate, TeamRoutineUpdate]):
    """Capa de servicio para rutinas compartidas en equipos."""


team_routine_service = TeamRoutineService(TeamRoutine)
