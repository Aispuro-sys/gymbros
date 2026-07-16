from app.api.crud_router import make_crud_router
from app.schemas.progress_photo_schema import (
    ProgressPhotoCreate,
    ProgressPhotoRead,
    ProgressPhotoUpdate,
)
from app.services.progress_photo_service import progress_photo_service

router = make_crud_router(
    prefix="/progress-photos",
    service=progress_photo_service,
    read_schema=ProgressPhotoRead,
    create_schema=ProgressPhotoCreate,
    update_schema=ProgressPhotoUpdate,
    tags=["progress-photos"],
)
