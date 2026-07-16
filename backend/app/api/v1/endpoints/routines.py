from app.api.crud_router import make_crud_router
from app.schemas.routine_schema import RoutineCreate, RoutineRead, RoutineUpdate
from app.services.routine_service import routine_service

router = make_crud_router(
    prefix="/routines",
    service=routine_service,
    read_schema=RoutineRead,
    create_schema=RoutineCreate,
    update_schema=RoutineUpdate,
    tags=["routines"],
)
