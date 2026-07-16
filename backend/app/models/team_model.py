from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, UniqueConstraint, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class Team(Base):
    __tablename__ = "teams"
    __table_args__ = (
        UniqueConstraint("invite_code", name="teams_invite_code_key"),
        Index("ix_teams_admin_id", "admin_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    admin_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    invite_code: Mapped[str] = mapped_column(String, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    admin: Mapped["User"] = relationship(back_populates="admin_teams")
    members: Mapped[list["TeamMember"]] = relationship(
        back_populates="team", cascade="all, delete-orphan"
    )
    team_routines: Mapped[list["TeamRoutine"]] = relationship(
        back_populates="team", cascade="all, delete-orphan"
    )
    posts: Mapped[list["TeamPost"]] = relationship(
        back_populates="team", cascade="all, delete-orphan"
    )
