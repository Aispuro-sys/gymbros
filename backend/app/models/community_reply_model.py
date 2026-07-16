from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, Text, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class CommunityReply(Base):
    __tablename__ = "community_replies"
    __table_args__ = (
        Index("ix_community_replies_post_id", "post_id"),
        Index("ix_community_replies_user_id", "user_id"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    post_id: Mapped[str] = mapped_column(
        String,
        ForeignKey("community_posts.id", ondelete="CASCADE", onupdate="CASCADE"),
        nullable=False,
    )
    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False
    )
    content: Mapped[str] = mapped_column(Text, nullable=False)
    media_url: Mapped[str | None] = mapped_column(String, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    post: Mapped["CommunityPost"] = relationship(back_populates="replies")
    user: Mapped["User"] = relationship(back_populates="community_replies")
    reactions: Mapped[list["CommunityReaction"]] = relationship(
        back_populates="reply", cascade="all, delete-orphan"
    )
