from app.models.team_post_model import TeamPost
from app.schemas.team_post_schema import TeamPostCreate, TeamPostUpdate
from app.services.base_service import CRUDBase


class TeamPostService(CRUDBase[TeamPost, TeamPostCreate, TeamPostUpdate]):
    """Capa de servicio para publicaciones de equipo."""


team_post_service = TeamPostService(TeamPost)
