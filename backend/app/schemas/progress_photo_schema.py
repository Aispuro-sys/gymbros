from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ProgressPhotoBase(BaseModel):
    user_id: str
    photo_url: str
    weight_logged: float | None = None
    date: datetime


class ProgressPhotoCreate(ProgressPhotoBase):
    pass


class ProgressPhotoUpdate(BaseModel):
    photo_url: str | None = None
    weight_logged: float | None = None
    date: datetime | None = None


class ProgressPhotoRead(ProgressPhotoBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
