CREATE DATABASE IF NOT EXISTS medisalud
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE medisalud;

SET @sentencia = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = 'medisalud' AND table_name = 'citas'
        )
        AND NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'medisalud' AND table_name = 'citas'
              AND column_name = 'motivo_cancelacion'
        ),
        'ALTER TABLE citas ADD COLUMN motivo_cancelacion VARCHAR(200) NULL',
        'SELECT 1'
    )
);

PREPARE aplicar FROM @sentencia;
EXECUTE aplicar;
DEALLOCATE PREPARE aplicar;

#