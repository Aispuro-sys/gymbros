from app.models.user_model import User
from app.schemas.user_schema import UserCreate, UserUpdate
from app.services.base_service import CRUDBase


class UserService(CRUDBase[User, UserCreate, UserUpdate]):
    """Capa de servicio para usuarios."""


user_service = UserService(User)
