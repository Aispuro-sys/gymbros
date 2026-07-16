from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Index, String, UniqueConstraint, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class ExerciseLog(Base):
    __tablename__ = "exercise_logs"
    __table_args__ = (
        UniqueConstraint(
            "user_id", "exercise_id", "date", name="exercise_logs_user_id_exercise_id_date_key"
        ),
        Index("ix_exercise_logs_user_id", "user_id"),
        Index("ix_exercise_logs_exercise_id", "exercise_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    exercise_id: Mapped[str] = mapped_column(
        String,
        ForeignKey("exercises.id", ondelete="CASCADE", onupdate="CASCADE"),
        nullable=False,
    )
    routine_id: Mapped[str] = mapped_column(String, nullable=False)
    completed: Mapped[bool] = mapped_column(Boolean, default=True)
    date: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    user: Mapped["User"] = relationship(back_populates="exercise_logs")
    exercise: Mapped["Exercise"] = relationship(back_populates="exercise_logs")
