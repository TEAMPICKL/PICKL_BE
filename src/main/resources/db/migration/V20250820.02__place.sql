CREATE TABLE IF NOT EXISTS place (
                                     id        BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name      VARCHAR(100) NOT NULL,
    brand     VARCHAR(60),
    category  ENUM('HYPERMARKET','SUPERMARKET') NOT NULL,
    address   VARCHAR(255),
    lat       DOUBLE NOT NULL,
    lng       DOUBLE NOT NULL,
    location  POINT   NOT NULL /*!80003 SRID 4326 */,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brand_category (brand, category),
    SPATIAL INDEX sidx_location (location)
    );