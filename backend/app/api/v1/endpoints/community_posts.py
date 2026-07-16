from app.api.crud_router import make_crud_router
from app.schemas.community_post_schema import (
    CommunityPostCreate,
    CommunityPostRead,
    CommunityPostUpdate,
)
from app.services.community_post_service import community_post_service

router = make_crud_router(
    prefix="/community-posts",
    service=community_post_service,
    read_schema=CommunityPostRead,
    create_schema=CommunityPostCreate,
    update_schema=CommunityPostUpdate,
    tags=["community-posts"],
)
