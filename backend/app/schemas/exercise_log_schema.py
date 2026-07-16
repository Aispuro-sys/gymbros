from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ExerciseLogBase(BaseModel):
    user_id: str
    exercise_id: str
    routine_id: str
    completed: bool = True
    date: datetime


class ExerciseLogCreate(ExerciseLogBase):
    pass


class ExerciseLogUpdate(BaseModel):
    exercise_id: str | None = None
    routine_id: str | None = None
    completed: bool | None = None
    date: datetime | None = None


class ExerciseLogRead(ExerciseLogBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
