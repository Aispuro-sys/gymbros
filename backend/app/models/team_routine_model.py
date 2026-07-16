from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class TeamRoutine(Base):
    __tablename__ = "team_routines"
    __table_args__ = (
        Index("ix_team_routines_team_id", "team_id"),
        Index("ix_team_routines_routine_id", "routine_id"),
        Index("ix_team_routines_shared_by", "shared_by"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    team_id: Mapped[str] = mapped_column(
        String, ForeignKey("teams.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    routine_id: Mapped[str] = mapped_column(
        String,
        ForeignKey("routines.id", ondelete="CASCADE", onupdate="CASCADE"),
        nullable=False,
    )
    shared_by: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    shared_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    team: Mapped["Team"] = relationship(back_populates="team_routines")
    routine: Mapped["Routine"] = relationship(back_populates="team_routines")
    shared_by_user: Mapped["User"] = relationship(back_populates="shared_team_routines")
