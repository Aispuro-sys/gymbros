from app.models.supplement_med_model import SupplementMed
from app.schemas.supplement_med_schema import SupplementMedCreate, SupplementMedUpdate
from app.services.base_service import CRUDBase


class SupplementMedService(CRUDBase[SupplementMed, SupplementMedCreate, SupplementMedUpdate]):
    """Capa de servicio para suplementos y medicamentos."""


supplement_med_service = SupplementMedService(SupplementMed)
