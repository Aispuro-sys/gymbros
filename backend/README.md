# Gymbros — Backend

Backend FastAPI + SQLAlchemy 2.0 + PostgreSQL para el proyecto Gymbros.

## Estructura

```
gymbros/backend/
├── alembic/                  # Migraciones con Alembic (placeholder)
├── app/
│   ├── __init__.py
│   ├── main.py               # Punto de entrada FastAPI
│   ├── api/
│   │   ├── deps.py           # Dependency get_db
│   │   ├── crud_router.py    # Generador de routers CRUD genéricos
│   │   └── v1/
│   │       ├── __init__.py
│   │       ├── api.py        # Agregador de routers v1
│   │       └── endpoints/    # Routers por dominio
│   │           ├── community_posts.py
│   │           ├── community_reactions.py
│   │           ├── community_replies.py
│   │           ├── exercise_logs.py
│   │           ├── exercises.py
│   │           ├── macros_daily_logs.py
│   │           ├── meals.py
│   │           ├── progress_photos.py
│   │           ├── recipes.py
│   │           ├── routines.py
│   │           ├── supplements_meds.py
│   │           ├── team_members.py
│   │           ├── team_posts.py
│   │           ├── team_routines.py
│   │           ├── teams.py
│   │           └── users.py
│   ├── core/                 # Configuración y utilidades core (vacío)
│   ├── db/
│   │   ├── __init__.py
│   │   └── session.py        # Engine y SessionLocal de SQLAlchemy
│   ├── docs/
│   │   └── Arranque_proyecto.md  # Esquema PostgreSQL original
│   ├── models/               # Modelos SQLAlchemy
│   │   ├── __init__.py
│   │   ├── base_model.py     # DeclarativeBase
│   │   ├── mixins.py         # Mixins reutilizables (id, timestamps)
│   │   ├── community_post_model.py
│   │   ├── community_reaction_model.py
│   │   ├── community_reply_model.py
│   │   ├── exercise_log_model.py
│   │   ├── exercise_model.py
│   │   ├── macros_daily_log_model.py
│   │   ├── meal_model.py
│   │   ├── progress_photo_model.py
│   │   ├── recipe_model.py
│   │   ├── routine_model.py
│   │   ├── supplement_med_model.py
│   │   ├── team_member_model.py
│   │   ├── team_model.py
│   │   ├── team_post_model.py
│   │   ├── team_routine_model.py
│   │   └── user_model.py
│   ├── schemas/              # Esquemas Pydantic
│   │   ├── __init__.py
│   │   ├── enums_schema.py   # Enums compartidos
│   │   ├── community_post_schema.py
│   │   ├── community_reaction_schema.py
│   │   ├── community_reply_schema.py
│   │   ├── exercise_log_schema.py
│   │   ├── exercise_schema.py
│   │   ├── macros_daily_log_schema.py
│   │   ├── meal_schema.py
│   │   ├── progress_photo_schema.py
│   │   ├── recipe_schema.py
│   │   ├── routine_schema.py
│   │   ├── supplement_med_schema.py
│   │   ├── team_member_schema.py
│   │   ├── team_post_schema.py
│   │   ├── team_routine_schema.py
│   │   ├── team_schema.py
│   │   └── user_schema.py
│   ├── services/             # Capa de servicios CRUD
│   │   ├── __init__.py
│   │   ├── base_service.py   # CRUDBase genérico
│   │   ├── community_post_service.py
│   │   ├── community_reaction_service.py
│   │   ├── community_reply_service.py
│   │   ├── exercise_log_service.py
│   │   ├── exercise_service.py
│   │   ├── macros_daily_log_service.py
│   │   ├── meal_service.py
│   │   ├── progress_photo_service.py
│   │   ├── recipe_service.py
│   │   ├── routine_service.py
│   │   ├── supplement_med_service.py
│   │   ├── team_member_service.py
│   │   ├── team_post_service.py
│   │   ├── team_routine_service.py
│   │   ├── team_service.py
│   │   └── user_service.py
│   └── utils/                # Utilidades (vacío)
├── docker-compose.yml        # Servicios db (Postgres) + backend
├── Dockerfile                # Imagen del backend FastAPI
├── requirements.txt          # Dependencias Python
├── .env.example              # Variables de entorno de ejemplo
├── scripts/                  # Scripts auxiliares (vacío)
└── tests/                    # Tests (vacío)
```

## Levantar el proyecto

```bash
cd gymbros/backend
cp .env.example .env
docker compose up -d --build
```

- API: http://localhost:8100
- Documentación: http://localhost:8100/docs
- PostgreSQL expuesto en el host: `localhost:15432`

El backend se ejecuta en el puerto interno `8000` y se mapea al host en `8100` para evitar conflictos con otros servicios.

## Convenciones

- **Modelos**: `app/models/<nombre>_model.py`
- **Esquemas**: `app/schemas/<nombre>_schema.py`
- **Servicios**: `app/services/<nombre>_service.py`
- **Routers**: `app/api/v1/endpoints/<nombre>.py`
- IDs tipo `TEXT` con `gen_random_uuid()` generado por PostgreSQL.
