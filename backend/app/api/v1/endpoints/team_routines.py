from app.api.crud_router import make_crud_router
from app.schemas.team_routine_schema import (
    TeamRoutineCreate,
    TeamRoutineRead,
    TeamRoutineUpdate,
)
from app.services.team_routine_service import team_routine_service

router = make_crud_router(
    prefix="/team-routines",
    service=team_routine_service,
    read_schema=TeamRoutineRead,
    create_schema=TeamRoutineCreate,
    update_schema=TeamRoutineUpdate,
    tags=["team-routines"],
)
