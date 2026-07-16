from pydantic import BaseModel, ConfigDict

from app.schemas.enums_schema import TeamMemberRole


class TeamMemberBase(BaseModel):
    team_id: str
    user_id: str
    role: TeamMemberRole = TeamMemberRole.MEMBER


class TeamMemberCreate(TeamMemberBase):
    pass


class TeamMemberUpdate(BaseModel):
    role: TeamMemberRole | None = None


class TeamMemberRead(TeamMemberBase):
    model_config = ConfigDict(from_attributes=True)

    id: str
