from datetime import datetime

from pydantic import BaseModel, ConfigDict


class TeamBase(BaseModel):
    name: str
    admin_id: str
    invite_code: str


class TeamCreate(TeamBase):
    pass


class TeamUpdate(BaseModel):
    name: str | None = None
    admin_id: str | None = None
    invite_code: str | None = None


class TeamRead(TeamBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
