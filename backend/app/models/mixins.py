from datetime import datetime

from sqlalchemy import String, func, text
from sqlalchemy.dialects.postgresql import TIMESTAMP
from sqlalchemy.orm import Mapped, mapped_column


class IDMixin:
    id: Mapped[str] = mapped_column(
        String,
        primary_key=True,
        server_default=text("gen_random_uuid()::text"),
    )


class CreatedAtMixin:
    created_at: Mapped[datetime] = mapped_column(
        TIMESTAMP(timezone=True, precision=3),
        server_default=func.now(),
        nullable=False,
    )


class UpdatedAtMixin:
    updated_at: Mapped[datetime] = mapped_column(
        TIMESTAMP(timezone=True, precision=3),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )
