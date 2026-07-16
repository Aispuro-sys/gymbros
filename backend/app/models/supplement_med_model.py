from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, ForeignKey, Index, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class SupplementMed(Base):
    __tablename__ = "supplements_meds"
    __table_args__ = (Index("ix_supplements_meds_user_id", "user_id"),)

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    dosage: Mapped[str] = mapped_column(String, nullable=False)
    time_of_day: Mapped[str] = mapped_column(String, default="MORNING")
    is_medication: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    user: Mapped["User"] = relationship(back_populates="supplements_meds")
