from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import MealType, RecipeSource


class RecipeBase(BaseModel):
    name: str
    description: str | None = None
    image_url: str | None = None
    calories: int = 0
    protein_g: int = 0
    carbs_g: int = 0
    fats_g: int = 0
    prep_time_min: int = 0
    servings: int = 1
    ingredients: list[str] | None = None
    instructions: list[str] | None = None
    meal_type: MealType = MealType.ANY
    diet_tags: list[str] | None = None
    source: RecipeSource = RecipeSource.COMMUNITY


class RecipeCreate(RecipeBase):
    pass


class RecipeUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    image_url: str | None = None
    calories: int | None = None
    protein_g: int | None = None
    carbs_g: int | None = None
    fats_g: int | None = None
    prep_time_min: int | None = None
    servings: int | None = None
    ingredients: list[str] | None = None
    instructions: list[str] | None = None
    meal_type: MealType | None = None
    diet_tags: list[str] | None = None
    source: RecipeSource | None = None


class RecipeRead(RecipeBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
    created_at: datetime
