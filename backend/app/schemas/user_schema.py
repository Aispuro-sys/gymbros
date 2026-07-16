from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr

from app.schemas.enums_schema import UserGender, UserGoal, UserRole


class UserBase(BaseModel):
    username: str
    email: EmailStr
    age: int | None = None
    height_cm: float | None = None
    weight_kg: float | None = None
    goal: UserGoal = UserGoal.MAINTENANCE
    body_type: str | None = None
    gender: UserGender = UserGender.M
    role: UserRole = UserRole.NORMAL
    bio: str | None = None
    profile_photo: str | None = None


class UserCreate(UserBase):
    password: str


class UserUpdate(BaseModel):
    username: str | None = None
    email: EmailStr | None = None
    age: int | None = None
    height_cm: float | None = None
    weight_kg: float | None = None
    goal: UserGoal | None = None
    body_type: str | None = None
    gender: UserGender | None = None
    role: UserRole | None = None
    bio: str | None = None
    profile_photo: str | None = None
    password: str | None = None


class UserRead(UserBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
    updated_at: datetime
