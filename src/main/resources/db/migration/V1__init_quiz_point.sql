-- Ingredient
CREATE TABLE IF NOT EXISTS ingredient (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(100) NOT NULL UNIQUE,
    icon_url VARCHAR(255)
    );

-- Quiz Pool (문항 풀)
CREATE TABLE IF NOT EXISTS quiz_pool (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         ingredient_id BIGINT NOT NULL,
                                         statement TEXT NOT NULL,
                                         answer BOOLEAN NOT NULL,                   -- true=O, false=X (MySQL에선 TINYINT(1))
                                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                         last_used_date DATE NULL,
                                         CONSTRAINT fk_quiz_pool_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
    );

CREATE INDEX idx_quiz_pool_active ON quiz_pool(is_active);
CREATE INDEX idx_quiz_pool_last_used ON quiz_pool(last_used_date);

-- Daily Quiz (하루 1문항)
CREATE TABLE IF NOT EXISTS daily_quiz (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          quiz_date DATE NOT NULL UNIQUE,
                                          quiz_pool_id BIGINT NOT NULL,
                                          ingredient_id BIGINT NOT NULL,
                                          statement TEXT NOT NULL,
                                          answer BOOLEAN NOT NULL,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          CONSTRAINT fk_daily_quiz_pool FOREIGN KEY (quiz_pool_id) REFERENCES quiz_pool(id),
    CONSTRAINT fk_daily_quiz_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
    );

-- Point Wallet / Tx
CREATE TABLE IF NOT EXISTS point_wallet (
                                            user_id BIGINT PRIMARY KEY,
                                            balance BIGINT NOT NULL DEFAULT 0,
                                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS point_tx (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        user_id BIGINT NOT NULL,
                                        amount BIGINT NOT NULL,                    -- +지급 / -차감
                                        reason VARCHAR(40) NOT NULL,               -- QUIZ_DAILY 등
    ref_id BIGINT NULL,                        -- quiz_attempt.id 등
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_point_tx_user (user_id),
    INDEX idx_point_tx_created (created_at)
    );

-- Attempt
CREATE TABLE IF NOT EXISTS quiz_attempt (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            user_id BIGINT NOT NULL,
                                            quiz_date DATE NOT NULL,
                                            attempt_no TINYINT NOT NULL DEFAULT 1,     -- 1 (필수), 확장 시 2
                                            answer BOOLEAN NOT NULL,
                                            is_correct BOOLEAN NOT NULL,
                                            points_awarded INT NOT NULL DEFAULT 0,
                                            answered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            UNIQUE KEY uq_quiz_attempt_user_date_no (user_id, quiz_date, attempt_no)
    );