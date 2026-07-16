from app.api.crud_router import make_crud_router
from app.schemas.supplement_med_schema import (
    SupplementMedCreate,
    SupplementMedRead,
    SupplementMedUpdate,
)
from app.services.supplement_med_service import supplement_med_service

router = make_crud_router(
    prefix="/supplements-meds",
    service=supplement_med_service,
    read_schema=SupplementMedRead,
    create_schema=SupplementMedCreate,
    update_schema=SupplementMedUpdate,
    tags=["supplements-meds"],
)
