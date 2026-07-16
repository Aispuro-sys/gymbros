from app.api.crud_router import make_crud_router
from app.schemas.user_schema import UserCreate, UserRead, UserUpdate
from app.services.user_service import user_service

router = make_crud_router(
    prefix="/users",
    service=user_service,
    read_schema=UserRead,
    create_schema=UserCreate,
    update_schema=UserUpdate,
    tags=["users"],
)
