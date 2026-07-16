from datetime import datetime

from pydantic import BaseModel, ConfigDict


class MacrosDailyLogBase(BaseModel):
    user_id: str
    date: datetime
    calories: int = 0
    protein_g: int = 0
    carbs_g: int = 0
    fats_g: int = 0


class MacrosDailyLogCreate(MacrosDailyLogBase):
    pass


class MacrosDailyLogUpdate(BaseModel):
    date: datetime | None = None
    calories: int | None = None
    protein_g: int | None = None
    carbs_g: int | None = None
    fats_g: int | None = None


class MacrosDailyLogRead(MacrosDailyLogBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
