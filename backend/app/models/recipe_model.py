from __future__ import annotations

from datetime import datetime

from sqlalchemy import ARRAY, DateTime, Index, Integer, String, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base_model import Base


class Recipe(Base):
    __tablename__ = "recipes"
    __table_args__ = (
        Index("ix_recipes_meal_type", "meal_type"),
        Index("ix_recipes_source", "source"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    description: Mapped[str | None] = mapped_column(Text, default=None)
    image_url: Mapped[str | None] = mapped_column(String, default=None)
    calories: Mapped[int] = mapped_column(Integer, default=0)
    protein_g: Mapped[int] = mapped_column(Integer, default=0)
    carbs_g: Mapped[int] = mapped_column(Integer, default=0)
    fats_g: Mapped[int] = mapped_column(Integer, default=0)
    prep_time_min: Mapped[int] = mapped_column(Integer, default=0)
    servings: Mapped[int] = mapped_column(Integer, default=1)
    ingredients: Mapped[list[str] | None] = mapped_column(ARRAY(String), default=None)
    instructions: Mapped[list[str] | None] = mapped_column(ARRAY(String), default=None)
    meal_type: Mapped[str] = mapped_column(String, default="ANY")
    diet_tags: Mapped[list[str] | None] = mapped_column(ARRAY(String), default=None)
    source: Mapped[str] = mapped_column(String, default="community")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
