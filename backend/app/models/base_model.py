from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    """Base declarativa para todos los modelos de SQLAlchemy."""

    def __repr__(self) -> str:
        attrs = ", ".join(
            f"{key}={getattr(self, key)!r}"
            for key in ("id", "name", "username")
            if hasattr(self, key)
        )
        return f"<{self.__class__.__name__}({attrs})>"
