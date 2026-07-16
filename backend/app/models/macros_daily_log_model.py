from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class MacrosDailyLog(Base):
    __tablename__ = "macros_daily_log"
    __table_args__ = (Index("ix_macros_daily_log_user_id", "user_id"),)

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    date: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    calories: Mapped[int] = mapped_column(default=0)
    protein_g: Mapped[int] = mapped_column(default=0)
    carbs_g: Mapped[int] = mapped_column(default=0)
    fats_g: Mapped[int] = mapped_column(default=0)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    user: Mapped["User"] = relationship(back_populates="macros_daily_logs")
