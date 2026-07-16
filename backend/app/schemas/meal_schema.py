from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import MealType


class MealBase(BaseModel):
    user_id: str
    name: str
    meal_type: MealType = MealType.SNACK
    calories: int = 0
    protein_g: int = 0
    carbs_g: int = 0
    fats_g: int = 0
    photo_url: str | None = None
    confirmed: bool = False
    date: datetime


class MealCreate(MealBase):
    pass


class MealUpdate(BaseModel):
    name: str | None = None
    meal_type: MealType | None = None
    calories: int | None = None
    protein_g: int | None = None
    carbs_g: int | None = None
    fats_g: int | None = None
    photo_url: str | None = None
    confirmed: bool | None = None
    date: datetime | None = None


class MealRead(MealBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
