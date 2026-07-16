from datetime import datetime

from pydantic import BaseModel, ConfigDict


class RoutineBase(BaseModel):
    user_id: str
    name: str
    ai_generated: bool = False
    ai_prompt: str | None = None
    day_of_week: int | None = None


class RoutineCreate(RoutineBase):
    pass


class RoutineUpdate(BaseModel):
    name: str | None = None
    ai_generated: bool | None = None
    ai_prompt: str | None = None
    day_of_week: int | None = None


class RoutineRead(RoutineBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
    updated_at: datetime
