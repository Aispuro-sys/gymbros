from app.models.macros_daily_log_model import MacrosDailyLog
from app.schemas.macros_daily_log_schema import (
    MacrosDailyLogCreate,
    MacrosDailyLogUpdate,
)
from app.services.base_service import CRUDBase


class MacrosDailyLogService(CRUDBase[MacrosDailyLog, MacrosDailyLogCreate, MacrosDailyLogUpdate]):
    """Capa de servicio para registros diarios de macros."""


macros_daily_log_service = MacrosDailyLogService(MacrosDailyLog)
