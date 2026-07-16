from app.api.crud_router import make_crud_router
from app.schemas.community_reaction_schema import (
    CommunityReactionCreate,
    CommunityReactionRead,
    CommunityReactionUpdate,
)
from app.services.community_reaction_service import community_reaction_service

router = make_crud_router(
    prefix="/community-reactions",
    service=community_reaction_service,
    read_schema=CommunityReactionRead,
    create_schema=CommunityReactionCreate,
    update_schema=CommunityReactionUpdate,
    tags=["community-reactions"],
)
