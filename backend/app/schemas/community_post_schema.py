from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import CommunityMediaType


class CommunityPostBase(BaseModel):
    user_id: str
    content: str
    media_url: str | None = None
    media_type: CommunityMediaType = CommunityMediaType.TEXT
    routine_id: str | None = None
    parent_id: str | None = None


class CommunityPostCreate(CommunityPostBase):
    pass


class CommunityPostUpdate(BaseModel):
    content: str | None = None
    media_url: str | None = None
    media_type: CommunityMediaType | None = None
    routine_id: str | None = None
    parent_id: str | None = None


class CommunityPostRead(CommunityPostBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
