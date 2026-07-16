from app.models.team_model import Team
from app.schemas.team_schema import TeamCreate, TeamUpdate
from app.services.base_service import CRUDBase


class TeamService(CRUDBase[Team, TeamCreate, TeamUpdate]):
    """Capa de servicio para equipos."""


team_service = TeamService(Team)
