from app.models.team_member_model import TeamMember
from app.schemas.team_member_schema import TeamMemberCreate, TeamMemberUpdate
from app.services.base_service import CRUDBase


class TeamMemberService(CRUDBase[TeamMember, TeamMemberCreate, TeamMemberUpdate]):
    """Capa de servicio para miembros de equipo."""


team_member_service = TeamMemberService(TeamMember)
