SET @db := DATABASE();

-- created_at
SELECT
    IF (EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = 'point_wallet'
          AND COLUMN_NAME = 'created_at'
    ),
        'SELECT 1',
        'ALTER TABLE point_wallet ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP'
    ) INTO @sql;
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- updated_at
SELECT
    IF (EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = 'point_wallet'
          AND COLUMN_NAME = 'updated_at'
    ),
        'SELECT 1',
        'ALTER TABLE point_wallet ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'
    ) INTO @sql;
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- version
SELECT
    IF (EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db
          AND TABLE_NAME = 'point_wallet'
          AND COLUMN_NAME = 'version'
    ),
        'SELECT 1',
        'ALTER TABLE point_wallet ADD COLUMN version BIGINT NOT NULL DEFAULT 0'
    ) INTO @sql;
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;