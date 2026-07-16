from app.models.community_post_model import CommunityPost
from app.schemas.community_post_schema import CommunityPostCreate, CommunityPostUpdate
from app.services.base_service import CRUDBase


class CommunityPostService(CRUDBase[CommunityPost, CommunityPostCreate, CommunityPostUpdate]):
    """Capa de servicio para publicaciones de comunidad."""


community_post_service = CommunityPostService(CommunityPost)
