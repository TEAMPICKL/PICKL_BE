SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ingredient'
      AND COLUMN_NAME = 'thumbnail_url'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE ingredient ADD COLUMN thumbnail_url VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ingredient'
      AND COLUMN_NAME = 'short_desc'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE ingredient ADD COLUMN short_desc VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;