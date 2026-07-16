from app.api.crud_router import make_crud_router
from app.schemas.community_reply_schema import (
    CommunityReplyCreate,
    CommunityReplyRead,
    CommunityReplyUpdate,
)
from app.services.community_reply_service import community_reply_service

router = make_crud_router(
    prefix="/community-replies",
    service=community_reply_service,
    read_schema=CommunityReplyRead,
    create_schema=CommunityReplyCreate,
    update_schema=CommunityReplyUpdate,
    tags=["community-replies"],
)
