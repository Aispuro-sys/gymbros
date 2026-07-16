from datetime import datetime

from pydantic import BaseModel, ConfigDict


class TeamRoutineBase(BaseModel):
    team_id: str
    routine_id: str
    shared_by: str


class TeamRoutineCreate(TeamRoutineBase):
    pass


class TeamRoutineUpdate(BaseModel):
    pass


class TeamRoutineRead(TeamRoutineBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    shared_at: datetime
