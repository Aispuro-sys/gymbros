from app.models.community_reaction_model import CommunityReaction
from app.schemas.community_reaction_schema import (
    CommunityReactionCreate,
    CommunityReactionUpdate,
)
from app.services.base_service import CRUDBase


class CommunityReactionService(
    CRUDBase[CommunityReaction, CommunityReactionCreate, CommunityReactionUpdate]
):
    """Capa de servicio para reacciones de comunidad."""


community_reaction_service = CommunityReactionService(CommunityReaction)
