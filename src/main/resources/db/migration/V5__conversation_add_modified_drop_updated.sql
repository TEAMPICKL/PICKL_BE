-- 대체: 컬럼 조건문 없이 테이블을 원하는 스키마로 보장
CREATE TABLE IF NOT EXISTS conversation (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            user_id BIGINT NOT NULL,
                                            title VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

-- (주의) 여기서는 updated_at을 드롭하지 않습니다.
-- 새로 만든 DB에는 updated_at이 없으니 굳이 ALTER가 필요 없습니다.