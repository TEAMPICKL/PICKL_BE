INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '토마토는 가열하면 리코펜의 체내 흡수가 더 잘 된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '토마토';

INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오이는 수분 함량이 95% 이상으로 매우 높다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '오이';

INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '냉동 보관한 식품은 항상 신선 식품보다 영양이 훨씬 떨어진다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '냉동식품';