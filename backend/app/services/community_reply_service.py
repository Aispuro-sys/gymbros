from app.models.community_reply_model import CommunityReply
from app.schemas.community_reply_schema import CommunityReplyCreate, CommunityReplyUpdate
from app.services.base_service import CRUDBase


class CommunityReplyService(CRUDBase[CommunityReply, CommunityReplyCreate, CommunityReplyUpdate]):
    """Capa de servicio para respuestas de comunidad."""


community_reply_service = CommunityReplyService(CommunityReply)
