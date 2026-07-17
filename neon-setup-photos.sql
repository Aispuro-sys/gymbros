-- ============================================================
-- SCRIPT SQL PARA NEON - Crear/verificar tablas de fotos
-- Ejecutar manualmente en el SQL Editor de Neon (neon.tech)
-- ============================================================

-- 1. Verificar si la columna profile_photo existe en users
-- Si no existe, agregarla:
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'profile_photo') THEN
        ALTER TABLE "users" ADD COLUMN "profile_photo" TEXT;
        RAISE NOTICE 'Columna profile_photo agregada a users';
    ELSE
        RAISE NOTICE 'Columna profile_photo ya existe en users';
    END IF;
END $$;

-- 2. Crear tabla progress_photos si no existe
CREATE TABLE IF NOT EXISTS "progress_photos" (
    "id" TEXT NOT NULL DEFAULT gen_random_uuid()::text,
    "user_id" TEXT NOT NULL,
    "photo_url" TEXT NOT NULL,
    "weight_logged" DOUBLE PRECISION,
    "date" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "progress_photos_pkey" PRIMARY KEY ("id")
);

-- 3. Crear foreign key si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'progress_photos_user_id_fkey') THEN
        ALTER TABLE "progress_photos" 
        ADD CONSTRAINT "progress_photos_user_id_fkey" 
        FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;
        RAISE NOTICE 'Foreign key agregada';
    ELSE
        RAISE NOTICE 'Foreign key ya existe';
    END IF;
END $$;

-- 4. Crear índice para buscar fotos por usuario
CREATE INDEX IF NOT EXISTS "progress_photos_user_id_idx" ON "progress_photos"("user_id");

-- 5. Verificar estructura final
SELECT 'users' as tabla, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'profile_photo'

UNION ALL

SELECT 'progress_photos' as tabla, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'progress_photos'
ORDER BY tabla, column_name;

-- 6. Verificar fotos existentes (opcional)
-- SELECT * FROM progress_photos ORDER BY date DESC LIMIT 10;
-- SELECT id, username, profile_photo FROM users WHERE profile_photo IS NOT NULL;
