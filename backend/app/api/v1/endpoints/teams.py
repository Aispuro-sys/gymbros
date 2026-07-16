from app.api.crud_router import make_crud_router
from app.schemas.team_schema import TeamCreate, TeamRead, TeamUpdate
from app.services.team_service import team_service

router = make_crud_router(
    prefix="/teams",
    service=team_service,
    read_schema=TeamRead,
    create_schema=TeamCreate,
    update_schema=TeamUpdate,
    tags=["teams"],
)
