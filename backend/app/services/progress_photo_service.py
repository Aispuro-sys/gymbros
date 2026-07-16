from app.models.progress_photo_model import ProgressPhoto
from app.schemas.progress_photo_schema import ProgressPhotoCreate, ProgressPhotoUpdate
from app.services.base_service import CRUDBase


class ProgressPhotoService(CRUDBase[ProgressPhoto, ProgressPhotoCreate, ProgressPhotoUpdate]):
    """Capa de servicio para fotos de progreso."""


progress_photo_service = ProgressPhotoService(ProgressPhoto)
