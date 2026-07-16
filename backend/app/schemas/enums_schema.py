from enum import Enum


class UserGoal(str, Enum):
    MAINTENANCE = "MAINTENANCE"
    WEIGHT_LOSS = "WEIGHT_LOSS"
    MUSCLE_GAIN = "MUSCLE_GAIN"


class UserGender(str, Enum):
    M = "M"
    F = "F"
    OTHER = "OTHER"


class UserRole(str, Enum):
    NORMAL = "NORMAL"
    ATHLETE = "ATHLETE"
    ADMIN = "ADMIN"


class MealType(str, Enum):
    BREAKFAST = "BREAKFAST"
    LUNCH = "LUNCH"
    DINNER = "DINNER"
    SNACK = "SNACK"
    POST_WORKOUT = "POST_WORKOUT"
    ANY = "ANY"


class TimeOfDay(str, Enum):
    MORNING = "MORNING"
    AFTERNOON = "AFTERNOON"
    EVENING = "EVENING"
    NIGHT = "NIGHT"


class TeamMemberRole(str, Enum):
    MEMBER = "MEMBER"
    ADMIN = "ADMIN"


class TeamPostType(str, Enum):
    MESSAGE = "MESSAGE"
    ROUTINE = "ROUTINE"
    PHOTO = "PHOTO"


class CommunityMediaType(str, Enum):
    TEXT = "TEXT"
    IMAGE = "IMAGE"
    VIDEO = "VIDEO"


class RecipeSource(str, Enum):
    COMMUNITY = "community"
    AI = "ai"
    OFFICIAL = "official"
