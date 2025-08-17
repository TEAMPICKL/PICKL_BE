-- 미역/대파/다시마/곶감 관련 문제 제거 (idempotent)
DELETE qp
FROM quiz_pool qp
JOIN ingredient i ON i.id = qp.ingredient_id
WHERE i.name IN ('미역','대파','다시마','곶감');