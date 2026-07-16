from __future__ import annotations

from sqlalchemy import ForeignKey, Index, String, UniqueConstraint, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class TeamMember(Base):
    __tablename__ = "team_members"
    __table_args__ = (
        UniqueConstraint("team_id", "user_id", name="team_members_team_id_user_id_key"),
        Index("ix_team_members_team_id", "team_id"),
        Index("ix_team_members_user_id", "user_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    team_id: Mapped[str] = mapped_column(
        String, ForeignKey("teams.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    role: Mapped[str] = mapped_column(String, default="MEMBER")

    team: Mapped["Team"] = relationship(back_populates="members")
    user: Mapped["User"] = relationship(back_populates="team_memberships")
