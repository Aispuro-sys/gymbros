from app.api.crud_router import make_crud_router
from app.schemas.team_member_schema import (
    TeamMemberCreate,
    TeamMemberRead,
    TeamMemberUpdate,
)
from app.services.team_member_service import team_member_service

router = make_crud_router(
    prefix="/team-members",
    service=team_member_service,
    read_schema=TeamMemberRead,
    create_schema=TeamMemberCreate,
    update_schema=TeamMemberUpdate,
    tags=["team-members"],
)
