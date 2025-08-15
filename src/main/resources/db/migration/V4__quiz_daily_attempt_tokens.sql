CREATE TABLE IF NOT EXISTS quiz_daily_attempt_tokens (
                                                         id BIGINT NOT NULL AUTO_INCREMENT,
                                                         user_id BIGINT NOT NULL,
                                                         token_date DATE NOT NULL,                 -- 로컬/UTC 기준 하루 키
                                                         tokens INT NOT NULL DEFAULT 0,            -- 남은 추가 시도 수
                                                         ad_grants INT NOT NULL DEFAULT 0,         -- 오늘 광고 보상 지급 횟수(보통 0 또는 1)
                                                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                         PRIMARY KEY (id),
    UNIQUE KEY uq_quiz_tokens_user_date (user_id, token_date),
    INDEX idx_quiz_tokens_user (user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;