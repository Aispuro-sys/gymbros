from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import TeamPostType


class TeamPostBase(BaseModel):
    team_id: str
    user_id: str
    content: str
    post_type: TeamPostType = TeamPostType.MESSAGE
    routine_id: str | None = None


class TeamPostCreate(TeamPostBase):
    pass


class TeamPostUpdate(BaseModel):
    content: str | None = None
    post_type: TeamPostType | None = None
    routine_id: str | None = None


class TeamPostRead(TeamPostBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
