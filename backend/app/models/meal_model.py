from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Index, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class Meal(Base):
    __tablename__ = "meals"
    __table_args__ = (
        Index("ix_meals_user_id", "user_id"),
        Index("ix_meals_date", "date"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    meal_type: Mapped[str] = mapped_column(String, default="SNACK")
    calories: Mapped[int] = mapped_column(default=0)
    protein_g: Mapped[int] = mapped_column(default=0)
    carbs_g: Mapped[int] = mapped_column(default=0)
    fats_g: Mapped[int] = mapped_column(default=0)
    photo_url: Mapped[str | None] = mapped_column(String, default=None)
    confirmed: Mapped[bool] = mapped_column(Boolean, default=False)
    date: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    user: Mapped["User"] = relationship(back_populates="meals")
