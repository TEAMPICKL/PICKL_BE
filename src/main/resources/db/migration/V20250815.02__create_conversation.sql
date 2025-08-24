CREATE TABLE IF NOT EXISTS conversation (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            user_id BIGINT NOT NULL,
                                            title VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_conversation_user_id (user_id)
    -- FK를 쓰려면 아래 주석 해제 (user 테이블/컬럼명 확인 필수)
    -- ,CONSTRAINT fk_conversation_user
    --   FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
    );