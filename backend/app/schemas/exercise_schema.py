from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ExerciseBase(BaseModel):
    routine_id: str
    name: str
    sets: int = 3
    reps: str = "8-12"
    rest_seconds: int = 90
    order_index: int = 0
    exercise_dataset_id: str | None = None


class ExerciseCreate(ExerciseBase):
    pass


class ExerciseUpdate(BaseModel):
    routine_id: str | None = None
    name: str | None = None
    sets: int | None = None
    reps: str | None = None
    rest_seconds: int | None = None
    order_index: int | None = None
    exercise_dataset_id: str | None = None


class ExerciseRead(ExerciseBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
