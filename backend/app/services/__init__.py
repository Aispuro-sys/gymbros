from app.services.base_service import CRUDBase
from app.services.user_service import UserService, user_service
from app.services.macros_daily_log_service import (
    MacrosDailyLogService,
    macros_daily_log_service,
)
from app.services.meal_service import MealService, meal_service
from app.services.routine_service import RoutineService, routine_service
from app.services.exercise_service import ExerciseService, exercise_service
from app.services.exercise_log_service import ExerciseLogService, exercise_log_service
from app.services.progress_photo_service import ProgressPhotoService, progress_photo_service
from app.services.supplement_med_service import (
    SupplementMedService,
    supplement_med_service,
)
from app.services.team_service import TeamService, team_service
from app.services.team_member_service import TeamMemberService, team_member_service
from app.services.team_routine_service import TeamRoutineService, team_routine_service
from app.services.team_post_service import TeamPostService, team_post_service
from app.services.recipe_service import RecipeService, recipe_service
from app.services.community_post_service import (
    CommunityPostService,
    community_post_service,
)
from app.services.community_reply_service import (
    CommunityReplyService,
    community_reply_service,
)
from app.services.community_reaction_service import (
    CommunityReactionService,
    community_reaction_service,
)

__all__ = [
    "CRUDBase",
    "UserService",
    "user_service",
    "MacrosDailyLogService",
    "macros_daily_log_service",
    "MealService",
    "meal_service",
    "RoutineService",
    "routine_service",
    "ExerciseService",
    "exercise_service",
    "ExerciseLogService",
    "exercise_log_service",
    "ProgressPhotoService",
    "progress_photo_service",
    "SupplementMedService",
    "supplement_med_service",
    "TeamService",
    "team_service",
    "TeamMemberService",
    "team_member_service",
    "TeamRoutineService",
    "team_routine_service",
    "TeamPostService",
    "team_post_service",
    "RecipeService",
    "recipe_service",
    "CommunityPostService",
    "community_post_service",
    "CommunityReplyService",
    "community_reply_service",
    "CommunityReactionService",
    "community_reaction_service",
]
