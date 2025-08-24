-- 1) quiz_attempt에 어떤 문제를 풀었는지 추적 컬럼 추가
ALTER TABLE quiz_attempt
    ADD COLUMN quiz_pool_id BIGINT NULL;

CREATE INDEX idx_quiz_attempt_user_date_no ON quiz_attempt (user_id, quiz_date, attempt_no);

-- (선택) 이미 존재하면 무시
-- 2) 사용자별-날짜별-시도번호에 배정된 문제를 고정 저장하는 테이블
CREATE TABLE IF NOT EXISTS user_daily_question (
                                                   user_id      BIGINT      NOT NULL,
                                                   quiz_date    DATE        NOT NULL,
                                                   attempt_no   TINYINT     NOT NULL,
                                                   quiz_pool_id BIGINT      NOT NULL,
                                                   created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   PRIMARY KEY (user_id, quiz_date, attempt_no)
    );