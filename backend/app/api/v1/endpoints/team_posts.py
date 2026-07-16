from app.api.crud_router import make_crud_router
from app.schemas.team_post_schema import TeamPostCreate, TeamPostRead, TeamPostUpdate
from app.services.team_post_service import team_post_service

router = make_crud_router(
    prefix="/team-posts",
    service=team_post_service,
    read_schema=TeamPostRead,
    create_schema=TeamPostCreate,
    update_schema=TeamPostUpdate,
    tags=["team-posts"],
)
