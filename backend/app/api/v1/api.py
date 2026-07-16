from fastapi import APIRouter

from app.api.v1.endpoints import (
    community_posts,
    community_reactions,
    community_replies,
    exercise_logs,
    exercises,
    macros_daily_logs,
    meals,
    progress_photos,
    recipes,
    routines,
    supplements_meds,
    team_members,
    team_posts,
    team_routines,
    teams,
    users,
)

api_router = APIRouter()

api_router.include_router(users.router)
api_router.include_router(macros_daily_logs.router)
api_router.include_router(meals.router)
api_router.include_router(routines.router)
api_router.include_router(exercises.router)
api_router.include_router(exercise_logs.router)
api_router.include_router(progress_photos.router)
api_router.include_router(supplements_meds.router)
api_router.include_router(teams.router)
api_router.include_router(team_members.router)
api_router.include_router(team_routines.router)
api_router.include_router(team_posts.router)
api_router.include_router(recipes.router)
api_router.include_router(community_posts.router)
api_router.include_router(community_replies.router)
api_router.include_router(community_reactions.router)
