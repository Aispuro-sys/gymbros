from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class TeamPost(Base):
    __tablename__ = "team_posts"
    __table_args__ = (
        Index("ix_team_posts_team_id", "team_id"),
        Index("ix_team_posts_user_id", "user_id"),
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
    content: Mapped[str] = mapped_column(Text, nullable=False)
    post_type: Mapped[str] = mapped_column(String, default="MESSAGE")
    routine_id: Mapped[str | None] = mapped_column(String, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    team: Mapped["Team"] = relationship(back_populates="posts")
    user: Mapped["User"] = relationship(back_populates="team_posts")
