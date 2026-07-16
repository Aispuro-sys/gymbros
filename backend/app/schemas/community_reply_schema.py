from datetime import datetime

from pydantic import BaseModel, ConfigDict


class CommunityReplyBase(BaseModel):
    post_id: str
    user_id: str
    content: str
    media_url: str | None = None


class CommunityReplyCreate(CommunityReplyBase):
    pass


class CommunityReplyUpdate(BaseModel):
    content: str | None = None
    media_url: str | None = None


class CommunityReplyRead(CommunityReplyBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
