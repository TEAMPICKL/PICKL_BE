-- 1) 새로 생성되는 지갑의 기본값을 30000으로
ALTER TABLE point_wallet
    MODIFY COLUMN balance BIGINT NOT NULL DEFAULT 30000;

-- 2) 기존 지갑이 있는 유저: 30000으로 보정하고 차액을 거래내역에 기록
--   (amount = 30000 - 현재잔액, 양수면 적립, 음수면 차감)
INSERT INTO point_tx (user_id, amount, reason, ref_id)
SELECT pw.user_id,
       (30000 - pw.balance) AS delta,
       'INIT_30000',
       NULL
FROM point_wallet pw
WHERE pw.balance <> 30000;

UPDATE point_wallet
SET balance = 30000
WHERE balance <> 30000;

-- 3) 지갑이 아예 없는 유저: 지갑 생성 + 30000 지급 & 거래내역 기롥
INSERT INTO point_wallet (user_id, balance)
SELECT u.id, 30000
FROM users u
         LEFT JOIN point_wallet pw ON pw.user_id = u.id
WHERE pw.user_id IS NULL;

INSERT INTO point_tx (user_id, amount, reason, ref_id)
SELECT u.id, 30000, 'INIT_30000', NULL
FROM users u
         LEFT JOIN point_wallet pw ON pw.user_id = u.id
WHERE pw.user_id IS NULL;