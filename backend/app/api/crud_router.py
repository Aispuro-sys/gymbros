from typing import TypeVar

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.services.base_service import CRUDBase

ModelType = TypeVar("ModelType")
CreateSchemaType = TypeVar("CreateSchemaType", bound=BaseModel)
UpdateSchemaType = TypeVar("UpdateSchemaType", bound=BaseModel)
ReadSchemaType = TypeVar("ReadSchemaType", bound=BaseModel)


def make_crud_router(
    prefix: str,
    service: CRUDBase,
    read_schema: type[ReadSchemaType],
    create_schema: type[CreateSchemaType],
    update_schema: type[UpdateSchemaType],
    tags: list[str] | None = None,
) -> APIRouter:
    """Genera un APIRouter con los endpoints CRUD básicos para un dominio."""
    router = APIRouter(prefix=prefix, tags=tags)

    @router.get("/{id}", response_model=read_schema)
    def read_one(id: str, db: Session = Depends(get_db)) -> ModelType:
        obj = service.get(db, id=id)
        if obj is None:
            raise HTTPException(status_code=404, detail="Not found")
        return obj

    @router.get("/", response_model=list[read_schema])
    def read_many(
        skip: int = 0, limit: int = 100, db: Session = Depends(get_db)
    ) -> list[ModelType]:
        return service.get_multi(db, skip=skip, limit=limit)

    @router.post("/", response_model=read_schema, status_code=201)
    def create(obj_in: create_schema, db: Session = Depends(get_db)) -> ModelType:
        return service.create(db, obj_in=obj_in)

    @router.put("/{id}", response_model=read_schema)
    def update(
        id: str, obj_in: update_schema, db: Session = Depends(get_db)
    ) -> ModelType:
        db_obj = service.get(db, id=id)
        if db_obj is None:
            raise HTTPException(status_code=404, detail="Not found")
        return service.update(db, db_obj=db_obj, obj_in=obj_in)

    @router.delete("/{id}", response_model=read_schema)
    def delete(id: str, db: Session = Depends(get_db)) -> ModelType:
        obj = service.remove(db, id=id)
        if obj is None:
            raise HTTPException(status_code=404, detail="Not found")
        return obj

    return router
