from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, Integer, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class Exercise(Base):
    __tablename__ = "exercises"
    __table_args__ = (Index("ix_exercises_routine_id", "routine_id"),)

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    routine_id: Mapped[str] = mapped_column(
        String, ForeignKey("routines.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    sets: Mapped[int] = mapped_column(Integer, default=3)
    reps: Mapped[str] = mapped_column(String, default="8-12")
    rest_seconds: Mapped[int] = mapped_column(Integer, default=90)
    order_index: Mapped[int] = mapped_column(Integer, default=0)
    exercise_dataset_id: Mapped[str | None] = mapped_column(String, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    routine: Mapped["Routine"] = relationship(back_populates="exercises")
    exercise_logs: Mapped[list["ExerciseLog"]] = relationship(
        back_populates="exercise", cascade="all, delete-orphan"
    )
