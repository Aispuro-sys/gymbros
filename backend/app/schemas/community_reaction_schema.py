from datetime import datetime

from pydantic import BaseModel, ConfigDict


class CommunityReactionBase(BaseModel):
    post_id: str | None = None
    reply_id: str | None = None
    user_id: str
    emoji: str


class CommunityReactionCreate(CommunityReactionBase):
    pass


class CommunityReactionUpdate(BaseModel):
    emoji: str | None = None


class CommunityReactionRead(CommunityReactionBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
