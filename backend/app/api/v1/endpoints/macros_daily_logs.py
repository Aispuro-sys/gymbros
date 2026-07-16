from app.api.crud_router import make_crud_router
from app.schemas.macros_daily_log_schema import (
    MacrosDailyLogCreate,
    MacrosDailyLogRead,
    MacrosDailyLogUpdate,
)
from app.services.macros_daily_log_service import macros_daily_log_service

router = make_crud_router(
    prefix="/macros-daily-logs",
    service=macros_daily_log_service,
    read_schema=MacrosDailyLogRead,
    create_schema=MacrosDailyLogCreate,
    update_schema=MacrosDailyLogUpdate,
    tags=["macros-daily-logs"],
)
