from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import DateTime, ForeignKey, Index, String, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class CommunityReaction(Base):
    __tablename__ = "community_reactions"
    __table_args__ = (
        Index("ix_community_reactions_user_id", "user_id"),
        Index("ix_community_reactions_post_id", "post_id"),
        Index("ix_community_reactions_reply_id", "reply_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    post_id: Mapped[str | None] = mapped_column(
        String,
        ForeignKey("community_posts.id", ondelete="CASCADE", onupdate="CASCADE"),
        default=None,
    )
    reply_id: Mapped[str | None] = mapped_column(
        String,
        ForeignKey("community_replies.id", ondelete="CASCADE", onupdate="CASCADE"),
        default=None,
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    emoji: Mapped[str] = mapped_column(String, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    post: Mapped[Optional["CommunityPost"]] = relationship(back_populates="reactions")
    reply: Mapped[Optional["CommunityReply"]] = relationship(back_populates="reactions")
    user: Mapped["User"] = relationship(back_populates="community_reactions")
