-- 포인트 지갑
CREATE TABLE IF NOT EXISTS wallet (
                                      user_id     BIGINT      PRIMARY KEY,
                                      balance     BIGINT      NOT NULL DEFAULT 0,
                                      updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      version     BIGINT      NOT NULL DEFAULT 0
);

-- 포인트 거래 내역
CREATE TABLE IF NOT EXISTS point_tx (
                                        id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        user_id       BIGINT      NOT NULL,
                                        amount        BIGINT      NOT NULL,      -- 적립은 +, 사용은 -
                                        type          VARCHAR(20) NOT NULL,      -- EARN / SPEND / ADJUST
    source        VARCHAR(30) NOT NULL,      -- QUIZ / ADMIN / ORDER 등
    ref_id        VARCHAR(64)     NULL,      -- 퀴즈id, 주문id 등 연관 키
    idempotency   VARCHAR(64)     NULL,      -- 멱등키(중복 방지)
    description   VARCHAR(255)    NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_source_ref (user_id, source, ref_id),
    UNIQUE KEY uk_idempotency (idempotency),
    KEY idx_user_created (user_id, created_at)
    );