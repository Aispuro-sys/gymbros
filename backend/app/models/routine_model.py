from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Index, Integer, String, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class Routine(Base):
    __tablename__ = "routines"
    __table_args__ = (
        Index("ix_routines_user_id", "user_id"),
        Index("ix_routines_day_of_week", "day_of_week"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    ai_generated: Mapped[bool] = mapped_column(Boolean, default=False)
    ai_prompt: Mapped[str | None] = mapped_column(Text, default=None)
    day_of_week: Mapped[int | None] = mapped_column(Integer, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )

    user: Mapped["User"] = relationship(back_populates="routines")
    exercises: Mapped[list["Exercise"]] = relationship(
        back_populates="routine", cascade="all, delete-orphan"
    )
    team_routines: Mapped[list["TeamRoutine"]] = relationship(
        back_populates="routine", cascade="all, delete-orphan"
    )
