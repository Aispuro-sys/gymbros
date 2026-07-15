-- ============================================
-- TALOS FORGE — Your Progress
-- Complete Database Schema (PostgreSQL / Neon)
-- ============================================

-- Drop existing tables (clean slate)
DROP TABLE IF EXISTS "community_reactions" CASCADE;
DROP TABLE IF EXISTS "community_replies" CASCADE;
DROP TABLE IF EXISTS "community_posts" CASCADE;
DROP TABLE IF EXISTS "recipes" CASCADE;
DROP TABLE IF EXISTS "meals" CASCADE;
DROP TABLE IF EXISTS "exercise_logs" CASCADE;
DROP TABLE IF EXISTS "team_posts" CASCADE;
DROP TABLE IF EXISTS "team_routines" CASCADE;
DROP TABLE IF EXISTS "team_members" CASCADE;
DROP TABLE IF EXISTS "teams" CASCADE;
DROP TABLE IF EXISTS "supplements_meds" CASCADE;
DROP TABLE IF EXISTS "progress_photos" CASCADE;
DROP TABLE IF EXISTS "exercises" CASCADE;
DROP TABLE IF EXISTS "routines" CASCADE;
DROP TABLE IF EXISTS "macros_daily_log" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;

-- ============================================
-- USERS
-- ============================================
CREATE TABLE "users" (
    "id"            TEXT NOT NULL,
    "username"      TEXT NOT NULL,
    "email"         TEXT NOT NULL,
    "password"      TEXT NOT NULL,
    "age"           INTEGER,
    "height_cm"     DOUBLE PRECISION,
    "weight_kg"     DOUBLE PRECISION,
    "goal"          TEXT DEFAULT 'MAINTENANCE',
    "body_type"     TEXT,
    "gender"        TEXT DEFAULT 'M',
    "role"          TEXT NOT NULL DEFAULT 'NORMAL',
    "bio"           TEXT,
    "profile_photo" TEXT,
    "created_at"    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at"    TIMESTAMP(3) NOT NULL,

    CONSTRAINT "users_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "users_username_key" ON "users"("username");
CREATE UNIQUE INDEX "users_email_key" ON "users"("email");

-- ============================================
-- MACROS DAILY LOG
-- ============================================
CREATE TABLE "macros_daily_log" (
    "id"         TEXT NOT NULL,
    "user_id"    TEXT NOT NULL,
    "date"       TIMESTAMP(3) NOT NULL,
    "calories"   INTEGER NOT NULL DEFAULT 0,
    "protein_g"  INTEGER NOT NULL DEFAULT 0,
    "carbs_g"    INTEGER NOT NULL DEFAULT 0,
    "fats_g"     INTEGER NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "macros_daily_log_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- MEALS
-- ============================================
CREATE TABLE "meals" (
    "id"         TEXT NOT NULL,
    "user_id"    TEXT NOT NULL,
    "name"       TEXT NOT NULL,
    "meal_type"  TEXT NOT NULL DEFAULT 'SNACK',
    "calories"   INTEGER NOT NULL DEFAULT 0,
    "protein_g"  INTEGER NOT NULL DEFAULT 0,
    "carbs_g"    INTEGER NOT NULL DEFAULT 0,
    "fats_g"     INTEGER NOT NULL DEFAULT 0,
    "photo_url"  TEXT,
    "confirmed"  BOOLEAN NOT NULL DEFAULT false,
    "date"       TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "meals_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- ROUTINES
-- ============================================
CREATE TABLE "routines" (
    "id"           TEXT NOT NULL,
    "user_id"      TEXT NOT NULL,
    "name"         TEXT NOT NULL,
    "ai_generated" BOOLEAN NOT NULL DEFAULT false,
    "ai_prompt"    TEXT,
    "day_of_week"  INTEGER,
    "created_at"   TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at"   TIMESTAMP(3) NOT NULL,

    CONSTRAINT "routines_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- EXERCISES
-- ============================================
CREATE TABLE "exercises" (
    "id"                  TEXT NOT NULL,
    "routine_id"          TEXT NOT NULL,
    "name"                TEXT NOT NULL,
    "sets"                INTEGER NOT NULL DEFAULT 3,
    "reps"                TEXT NOT NULL DEFAULT '8-12',
    "rest_seconds"        INTEGER NOT NULL DEFAULT 90,
    "order_index"         INTEGER NOT NULL DEFAULT 0,
    "exercise_dataset_id" TEXT,
    "created_at"          TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "exercises_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- EXERCISE LOGS
-- ============================================
CREATE TABLE "exercise_logs" (
    "id"          TEXT NOT NULL,
    "user_id"     TEXT NOT NULL,
    "exercise_id" TEXT NOT NULL,
    "routine_id"  TEXT NOT NULL,
    "completed"   BOOLEAN NOT NULL DEFAULT true,
    "date"        TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "exercise_logs_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "exercise_logs_user_id_exercise_id_date_key" ON "exercise_logs"("user_id", "exercise_id", "date");

-- ============================================
-- PROGRESS PHOTOS
-- ============================================
CREATE TABLE "progress_photos" (
    "id"            TEXT NOT NULL,
    "user_id"       TEXT NOT NULL,
    "photo_url"     TEXT NOT NULL,
    "weight_logged" DOUBLE PRECISION,
    "date"          TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "created_at"    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "progress_photos_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- SUPPLEMENTS / MEDS
-- ============================================
CREATE TABLE "supplements_meds" (
    "id"            TEXT NOT NULL,
    "user_id"       TEXT NOT NULL,
    "name"          TEXT NOT NULL,
    "dosage"        TEXT NOT NULL,
    "time_of_day"   TEXT NOT NULL DEFAULT 'MORNING',
    "is_medication" BOOLEAN NOT NULL DEFAULT false,
    "created_at"    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "supplements_meds_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- TEAMS
-- ============================================
CREATE TABLE "teams" (
    "id"          TEXT NOT NULL,
    "name"        TEXT NOT NULL,
    "admin_id"    TEXT NOT NULL,
    "invite_code" TEXT NOT NULL,
    "created_at"  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "teams_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "teams_invite_code_key" ON "teams"("invite_code");

-- ============================================
-- TEAM MEMBERS
-- ============================================
CREATE TABLE "team_members" (
    "id"      TEXT NOT NULL,
    "team_id" TEXT NOT NULL,
    "user_id" TEXT NOT NULL,
    "role"    TEXT NOT NULL DEFAULT 'MEMBER',

    CONSTRAINT "team_members_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "team_members_team_id_user_id_key" ON "team_members"("team_id", "user_id");

-- ============================================
-- TEAM ROUTINES (shared)
-- ============================================
CREATE TABLE "team_routines" (
    "id"         TEXT NOT NULL,
    "team_id"    TEXT NOT NULL,
    "routine_id" TEXT NOT NULL,
    "shared_by"  TEXT NOT NULL,
    "shared_at"  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "team_routines_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- TEAM POSTS
-- ============================================
CREATE TABLE "team_posts" (
    "id"         TEXT NOT NULL,
    "team_id"    TEXT NOT NULL,
    "user_id"    TEXT NOT NULL,
    "content"    TEXT NOT NULL,
    "post_type"  TEXT NOT NULL DEFAULT 'MESSAGE',
    "routine_id" TEXT,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "team_posts_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- RECIPES
-- ============================================
CREATE TABLE "recipes" (
    "id"            TEXT NOT NULL,
    "name"          TEXT NOT NULL,
    "description"   TEXT,
    "image_url"     TEXT,
    "calories"      INTEGER NOT NULL DEFAULT 0,
    "protein_g"     INTEGER NOT NULL DEFAULT 0,
    "carbs_g"       INTEGER NOT NULL DEFAULT 0,
    "fats_g"        INTEGER NOT NULL DEFAULT 0,
    "prep_time_min" INTEGER NOT NULL DEFAULT 0,
    "servings"      INTEGER NOT NULL DEFAULT 1,
    "ingredients"   TEXT[],
    "instructions"  TEXT[],
    "meal_type"     TEXT NOT NULL DEFAULT 'ANY',
    "diet_tags"     TEXT[],
    "source"        TEXT NOT NULL DEFAULT 'community',
    "created_at"    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "recipes_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- COMMUNITY POSTS
-- ============================================
CREATE TABLE "community_posts" (
    "id"         TEXT NOT NULL,
    "user_id"    TEXT NOT NULL,
    "content"    TEXT NOT NULL,
    "media_url"  TEXT,
    "media_type" TEXT NOT NULL DEFAULT 'TEXT',
    "routine_id" TEXT,
    "parent_id"  TEXT,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "community_posts_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- COMMUNITY REPLIES
-- ============================================
CREATE TABLE "community_replies" (
    "id"         TEXT NOT NULL,
    "post_id"    TEXT NOT NULL,
    "user_id"    TEXT NOT NULL,
    "content"    TEXT NOT NULL,
    "media_url"  TEXT,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "community_replies_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- COMMUNITY REACTIONS
-- ============================================
CREATE TABLE "community_reactions" (
    "id"         TEXT NOT NULL,
    "post_id"    TEXT,
    "reply_id"   TEXT,
    "user_id"    TEXT NOT NULL,
    "emoji"      TEXT NOT NULL,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "community_reactions_pkey" PRIMARY KEY ("id")
);

-- ============================================
-- FOREIGN KEYS
-- ============================================
ALTER TABLE "macros_daily_log" ADD CONSTRAINT "macros_daily_log_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "meals" ADD CONSTRAINT "meals_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "routines" ADD CONSTRAINT "routines_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "exercises" ADD CONSTRAINT "exercises_routine_id_fkey" FOREIGN KEY ("routine_id") REFERENCES "routines"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "exercise_logs" ADD CONSTRAINT "exercise_logs_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "exercise_logs" ADD CONSTRAINT "exercise_logs_exercise_id_fkey" FOREIGN KEY ("exercise_id") REFERENCES "exercises"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "progress_photos" ADD CONSTRAINT "progress_photos_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "supplements_meds" ADD CONSTRAINT "supplements_meds_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "teams" ADD CONSTRAINT "teams_admin_id_fkey" FOREIGN KEY ("admin_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_members" ADD CONSTRAINT "team_members_team_id_fkey" FOREIGN KEY ("team_id") REFERENCES "teams"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_members" ADD CONSTRAINT "team_members_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_routines" ADD CONSTRAINT "team_routines_team_id_fkey" FOREIGN KEY ("team_id") REFERENCES "teams"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_routines" ADD CONSTRAINT "team_routines_routine_id_fkey" FOREIGN KEY ("routine_id") REFERENCES "routines"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_routines" ADD CONSTRAINT "team_routines_shared_by_fkey" FOREIGN KEY ("shared_by") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_posts" ADD CONSTRAINT "team_posts_team_id_fkey" FOREIGN KEY ("team_id") REFERENCES "teams"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "team_posts" ADD CONSTRAINT "team_posts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_posts" ADD CONSTRAINT "community_posts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_replies" ADD CONSTRAINT "community_replies_post_id_fkey" FOREIGN KEY ("post_id") REFERENCES "community_posts"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_replies" ADD CONSTRAINT "community_replies_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_reactions" ADD CONSTRAINT "community_reactions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_reactions" ADD CONSTRAINT "community_reactions_post_id_fkey" FOREIGN KEY ("post_id") REFERENCES "community_posts"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "community_reactions" ADD CONSTRAINT "community_reactions_reply_id_fkey" FOREIGN KEY ("reply_id") REFERENCES "community_replies"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ============================================
-- SEED: FITNESS RECIPES
-- ============================================
INSERT INTO "recipes" ("id", "name", "description", "calories", "protein_g", "carbs_g", "fats_g", "prep_time_min", "servings", "ingredients", "instructions", "meal_type", "diet_tags", "source") VALUES
(gen_random_uuid(), 'Pollo a la Plancha con Arroz', 'Clásico de dieta fitness: pechuga de pollo magra con arroz blanco.', 450, 45, 50, 8, 25, 1, ARRAY['200g pechuga de pollo','1 taza arroz blanco','1 cda aceite de oliva','Sal, pimienta, ajo en polvo'], ARRAY['Cocinar el arroz según instrucciones.','Sazonar el pollo con sal, pimienta y ajo.','Calentar aceite y cocinar el pollo 5 min por lado.','Servir juntos.'], 'LUNCH', ARRAY['high-protein','low-fat'], 'community'),
(gen_random_uuid(), 'Avena con Proteína y Plátano', 'Desayuno alto en proteína con avena, whey protein y plátano.', 380, 30, 55, 6, 10, 1, ARRAY['50g avena','1 scoop proteína vainilla','1 plátano','200ml leche de almendras','Canela'], ARRAY['Cocinar la avena con la leche.','Mezclar la proteína con agua.','Combinar avena con proteína.','Topping con plátano y canela.'], 'BREAKFAST', ARRAY['high-protein','pre-workout'], 'community'),
(gen_random_uuid(), 'Ensalada de Atún con Huevo', 'Ensalada rápida y alta en proteína.', 320, 35, 12, 14, 10, 1, ARRAY['1 lata atún en agua','2 huevos cocidos','Lechuga, tomate, cebolla','1 cda aceite de oliva','Limón y sal'], ARRAY['Cocer los huevos 10 min.','Mezclar atún con huevos picados.','Agregar vegetales.','Aliñar con aceite, limón y sal.'], 'LUNCH', ARRAY['high-protein','low-carb','keto'], 'community'),
(gen_random_uuid(), 'Batido Post-Entreno', 'Batido de recuperación con proteína, avena y fruta.', 290, 28, 38, 4, 5, 1, ARRAY['1 scoop proteína chocolate','1/2 taza avena','1/2 plátano','200ml leche','1 cda crema de cacahuate'], ARRAY['Licuar todo junto.','Servir frío.'], 'POST_WORKOUT', ARRAY['high-protein','post-workout'], 'community'),
(gen_random_uuid(), 'Salmón al Horno con Batata', 'Salmón rico en omega-3 con batata asada.', 520, 38, 45, 18, 35, 1, ARRAY['180g filete de salmón','1 batata mediana','1 cda aceite de oliva','Eneldo, sal, limón'], ARRAY['Precalentar horno a 200°C.','Cortar batata en cubos y hornear 20 min.','Sazonar el salmón con eneldo y limón.','Hornear salmón 12-15 min.','Servir.'], 'DINNER', ARRAY['high-protein','omega-3'], 'community'),
(gen_random_uuid(), 'Yogur Griego con Frutos Rojos', 'Snack alto en proteína con yogur griego.', 180, 18, 22, 3, 5, 1, ARRAY['200g yogur griego natural','1/2 taza frutos rojos','1 cda miel','1 cda chía'], ARRAY['Mezclar yogur con miel.','Agregar frutos rojos.','Espolvorear chía.'], 'SNACK', ARRAY['high-protein','low-fat'], 'community'),
(gen_random_uuid(), 'Wrap de Pavo y Aguacate', 'Wrap rápido con pechuga de pavo y aguacate.', 340, 28, 35, 12, 10, 1, ARRAY['1 tortilla integral','100g pechuga de pavo','1/4 aguacate','Lechuga, tomate','Mostaza'], ARRAY['Untar aguacate en la tortilla.','Agregar pavo, lechuga y tomate.','Enrollar y cortar.'], 'LUNCH', ARRAY['high-protein','quick'], 'community'),
(gen_random_uuid(), 'Tofu Salteado con Vegetales', 'Salteado vegetariano alto en proteína.', 280, 22, 25, 10, 20, 1, ARRAY['200g tofu firme','Brócoli, pimiento, zanahoria','2 cda salsa de soya','1 cda aceite de sésamo','Ajo y jengibre'], ARRAY['Pressar y cortar tofu.','Saltear tofu hasta dorar.','Agregar vegetales 5 min.','Sazonar con soya, ajo y jengibre.'], 'DINNER', ARRAY['vegetarian','high-protein','vegan'], 'community'),
(gen_random_uuid(), 'Huevos Revueltos con Espinaca', 'Desayuno bajo en carbohidratos.', 240, 20, 6, 15, 10, 1, ARRAY['3 huevos','1 puñado espinaca','1 cda aceite de oliva','Sal y pimienta'], ARRAY['Calentar aceite.','Saltear espinaca 1 min.','Agregar huevos batidos y revolver.','Sazonar y servir.'], 'BREAKFAST', ARRAY['low-carb','keto','high-protein'], 'community'),
(gen_random_uuid(), 'Fajitas de Pollo', 'Fajitas fitness con pollo, pimientos y especias.', 400, 40, 30, 12, 25, 1, ARRAY['200g pechuga en tiras','1 pimiento rojo','1 pimiento verde','1/2 cebolla','Tortillas integrales','Comino, pimentón, sal'], ARRAY['Saltear el pollo hasta dorar.','Agregar pimientos y cebolla.','Sazonar con comino y pimentón.','Servir en tortillas.'], 'DINNER', ARRAY['high-protein','low-fat'], 'community'),
(gen_random_uuid(), 'Bowl de Quinoa con Pollo', 'Bowl completo con quinoa, pollo y vegetales.', 480, 38, 52, 14, 30, 1, ARRAY['1/2 taza quinoa','150g pollo cocido','1/4 aguacate','Tomate, pepino, maíz','Limón, sal, cilantro'], ARRAY['Cocinar quinoa 15 min.','Preparar pollo a la plancha.','Combinar todo en un bowl.','Aliñar con limón y cilantro.'], 'LUNCH', ARRAY['high-protein','gluten-free'], 'community'),
(gen_random_uuid(), 'Cena Ligera de Merluza', 'Filete de merluza al vapor con vegetales.', 260, 32, 15, 6, 20, 1, ARRAY['200g filete de merluza','Calabacín, zanahoria, brócoli','1 cda aceite de oliva','Limón, sal, eneldo'], ARRAY['Cocinar merluza al vapor 10 min.','Saltear vegetales 5 min.','Servir con aceite y limón.'], 'DINNER', ARRAY['low-calorie','high-protein','low-fat'], 'community');

-- ============================================
-- SEED: DEMO USERS (passwords are bcrypt hashed)
-- admin@olympus.com / admin123
-- trainer@olympus.com / trainer123
-- client@olympus.com / client123
-- ============================================
-- Run this AFTER the app has been deployed at least once so prisma generates the hash,
-- OR use the registration endpoint to create users, then run:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@olympus.com';
-- UPDATE users SET role = 'ATHLETE' WHERE email = 'trainer@olympus.com';