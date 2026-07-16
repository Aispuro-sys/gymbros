from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import TimeOfDay


class SupplementMedBase(BaseModel):
    user_id: str
    name: str
    dosage: str
    time_of_day: TimeOfDay = TimeOfDay.MORNING
    is_medication: bool = False


class SupplementMedCreate(SupplementMedBase):
    pass


class SupplementMedUpdate(BaseModel):
    name: str | None = None
    dosage: str | None = None
    time_of_day: TimeOfDay | None = None
    is_medication: bool | None = None


class SupplementMedRead(SupplementMedBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
