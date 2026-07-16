from __future__ import annotations

from datetime import datetime

from sqlalchemy import DateTime, Index, String, Text, UniqueConstraint, func, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base_model import Base


class User(Base):
    __tablename__ = "users"
    __table_args__ = (
        UniqueConstraint("username", name="users_username_key"),
        UniqueConstraint("email", name="users_email_key"),
        Index("ix_users_role", "role"),
    )

    id: Mapped[str] = mapped_column(
        String, primary_key=True, server_default=text("gen_random_uuid()::text")
    )
    username: Mapped[str] = mapped_column(String, nullable=False)
    email: Mapped[str] = mapped_column(String, nullable=False)
    password: Mapped[str] = mapped_column(String, nullable=False)
    age: Mapped[int | None] = mapped_column(default=None)
    height_cm: Mapped[float | None] = mapped_column(default=None)
    weight_kg: Mapped[float | None] = mapped_column(default=None)
    goal: Mapped[str] = mapped_column(String, default="MAINTENANCE")
    body_type: Mapped[str | None] = mapped_column(String, default=None)
    gender: Mapped[str] = mapped_column(String, default="M")
    role: Mapped[str] = mapped_column(String, default="NORMAL")
    bio: Mapped[str | None] = mapped_column(Text, default=None)
    profile_photo: Mapped[str | None] = mapped_column(String, default=None)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )

    macros_daily_logs: Mapped[list["MacrosDailyLog"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    meals: Mapped[list["Meal"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    routines: Mapped[list["Routine"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    exercise_logs: Mapped[list["ExerciseLog"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    progress_photos: Mapped[list["ProgressPhoto"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    supplements_meds: Mapped[list["SupplementMed"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    admin_teams: Mapped[list["Team"]] = relationship(
        back_populates="admin", cascade="all, delete-orphan"
    )
    team_memberships: Mapped[list["TeamMember"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    shared_team_routines: Mapped[list["TeamRoutine"]] = relationship(
        back_populates="shared_by_user", cascade="all, delete-orphan"
    )
    team_posts: Mapped[list["TeamPost"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    community_posts: Mapped[list["CommunityPost"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    community_replies: Mapped[list["CommunityReply"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
    community_reactions: Mapped[list["CommunityReaction"]] = relationship(
        back_populates="user", cascade="all, delete-orphan"
    )
