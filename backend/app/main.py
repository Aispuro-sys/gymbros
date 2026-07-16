from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.v1 import api_router
from app.db.session import engine
from app.models.base_model import Base


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title="Gymbros API", version="0.1.0", lifespan=lifespan)
app.include_router(api_router, prefix="/api/v1")


@app.get("/health")
def health_check():
    return {"status": "ok"}
