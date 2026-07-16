from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class CommunityPost(Base):
    __tablename__ = "community_posts"
    __table_args__ = (
        Index("ix_community_posts_user_id", "user_id"),
        Index("ix_community_posts_routine_id", "routine_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    content: Mapped[str] = mapped_column(Text, nullable=False)
    media_url: Mapped[str | None] = mapped_column(String, default=None)
    media_type: Mapped[str] = mapped_column(String, default="TEXT")
    routine_id: Mapped[str | None] = mapped_column(String, default=None)
    parent_id: Mapped[str | None] = mapped_column(String, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    user: Mapped["User"] = relationship(back_populates="community_posts")
    replies: Mapped[list["CommunityReply"]] = relationship(
        back_populates="post", cascade="all, delete-orphan"
    )
    reactions: Mapped[list["CommunityReaction"]] = relationship(
        back_populates="post", cascade="all, delete-orphan"
    )
