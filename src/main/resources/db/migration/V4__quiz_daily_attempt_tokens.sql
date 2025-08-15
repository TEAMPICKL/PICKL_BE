CREATE TABLE IF NOT EXISTS quiz_daily_attempt (
                                                  user_id        BIGINT  NOT NULL,
                                                  quiz_date      DATE    NOT NULL,
                                                  attempts_used  INT     NOT NULL DEFAULT 0,   -- 오늘 제출한 횟수
                                                  bonus_tokens   INT     NOT NULL DEFAULT 0,   -- 광고로 얻은 추가 시도권 수
                                                  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                  ON UPDATE CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (user_id, quiz_date)
    );