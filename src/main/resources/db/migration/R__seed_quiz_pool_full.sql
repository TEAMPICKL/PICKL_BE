-- =========================================================
-- 일일 O/X 퀴즈 시드 데이터 (idempotent)
-- 각 INSERT는 동일 statement 중복을 NOT EXISTS로 방지
-- =========================================================

/* ===================== 🍅 토마토 ===================== */
-- ① 비타민 C·칼륨 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '토마토는 비타민 C와 칼륨이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '토마토'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '토마토는 비타민 C와 칼륨이 풍부하다.');

-- ② 가열 시 리코펜 흡수 ↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '토마토는 가열하면 리코펜의 체내 흡수가 더 잘 된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '토마토'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '토마토는 가열하면 리코펜의 체내 흡수가 더 잘 된다.');

-- ③ 과일이 아니라 채소다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '토마토는 과일이 아니라 채소다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '토마토'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '토마토는 과일이 아니라 채소다.');


/* ===================== 🥒 오이 ===================== */
-- ④ 수분 95%+ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오이는 수분 함량이 95% 이상으로 매우 높다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '오이'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '오이는 수분 함량이 95% 이상으로 매우 높다.');

-- ⑤ 껍질째 먹으면 비타민 K 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오이는 껍질째 먹으면 비타민 K 섭취에 도움이 된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '오이'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '오이는 껍질째 먹으면 비타민 K 섭취에 도움이 된다.');

-- ⑥ 반드시 냉동 보관 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오이는 보관 시 반드시 냉동실에 넣어야 한다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '오이'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '오이는 보관 시 반드시 냉동실에 넣어야 한다.');


/* ===================== 🥕 당근 ===================== */
-- ⑦ 베타카로틴 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '당근에는 베타카로틴이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '당근'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '당근에는 베타카로틴이 풍부하다.');

-- ⑧ 익히면 흡수율 ↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '당근을 익히면 베타카로틴 흡수율이 올라간다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '당근'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '당근을 익히면 베타카로틴 흡수율이 올라간다.');

-- ⑨ 냉동하면 영양소 사라짐 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '당근은 냉동 보관하면 영양소가 사라진다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '당근'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '당근은 냉동 보관하면 영양소가 사라진다.');

-- ⑩ 기름과 함께 먹으면 흡수 ↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '당근은 기름과 함께 먹으면 베타카로틴 흡수가 더 좋아진다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '당근'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '당근은 기름과 함께 먹으면 베타카로틴 흡수가 더 좋아진다.');


/* ===================== 🥔 감자 ===================== */
-- ⑪ 빛 노출 시 솔라닌 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '감자는 빛에 노출되면 독성물질 솔라닌이 생긴다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '감자'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '감자는 빛에 노출되면 독성물질 솔라닌이 생긴다.');

-- ⑫ 비타민 C 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '감자는 비타민 C가 많아 항산화에 좋다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '감자'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '감자는 비타민 C가 많아 항산화에 좋다.');

-- ⑬ 껍질 벗겨 보관 필수 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '감자는 반드시 껍질을 벗겨서 보관해야 한다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '감자'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '감자는 반드시 껍질을 벗겨서 보관해야 한다.');

-- ⑭ 감자엔 비타민C가 거의 없다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '감자는 비타민C가 거의 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '감자'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '감자는 비타민C가 거의 없다.');

-- ⑮ 싹 난 감자, 솔라닌 주의 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '감자는 싹이 나면 솔라닌 독성에 주의해야 한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '감자'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '감자는 싹이 나면 솔라닌 독성에 주의해야 한다.');


/* ===================== 🌽 옥수수 ===================== */
-- ⑯ 식이섬유 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '옥수수는 식이섬유가 풍부해 변비 예방에 도움을 준다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '옥수수'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '옥수수는 식이섬유가 풍부해 변비 예방에 도움을 준다.');

-- ⑰ 단백질 전혀 없다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '옥수수는 단백질이 전혀 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '옥수수'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '옥수수는 단백질이 전혀 없다.');

-- ⑱ 껍질째 삶으면 손실↓ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '옥수수는 껍질째 삶으면 영양소 손실이 적다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '옥수수'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '옥수수는 껍질째 삶으면 영양소 손실이 적다.');


/* ===================== 🍎 사과 ===================== */
-- ⑲ 껍질=폴리페놀/섬유 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '사과 껍질에는 폴리페놀과 식이섬유가 많다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '사과'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '사과 껍질에는 폴리페놀과 식이섬유가 많다.');

-- ⑳ 깎아 두면 비타민C 파괴↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '사과를 깎아 두면 비타민 C가 빨리 파괴된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '사과'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '사과를 깎아 두면 비타민 C가 빨리 파괴된다.');

-- ㉑ 사과씨 먹어도 전혀 문제없다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '사과씨는 먹어도 전혀 문제없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '사과'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '사과씨는 먹어도 전혀 문제없다.');

-- ㉒ 껍질=식이섬유·폴리페놀 많다 (T) ※유사 문구 별도 관리
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '사과 껍질에는 식이섬유와 폴리페놀 등이 많다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '사과'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '사과 껍질에는 식이섬유와 폴리페놀 등이 많다.');


/* ===================== 🥬 시금치 ===================== */
-- ㉓ 철분·엽산 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '시금치에는 철분과 엽산이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '시금치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '시금치에는 철분과 엽산이 풍부하다.');

-- ㉔ 오래 삶으면 철 손실↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '시금치를 오래 삶으면 철분 손실이 커진다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '시금치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '시금치를 오래 삶으면 철분 손실이 커진다.');

-- ㉕ 날로 먹는 게 가장 좋다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '시금치는 날로 먹는 것이 영양소 흡수에 가장 좋다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '시금치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '시금치는 날로 먹는 것이 영양소 흡수에 가장 좋다.');

-- ㉖ 옥살산이 칼슘 흡수 100% 차단 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '시금치는 옥살산 때문에 칼슘 흡수를 100% 차단한다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '시금치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '시금치는 옥살산 때문에 칼슘 흡수를 100% 차단한다.');


/* ===================== 🥦 브로콜리 ===================== */
-- ㉗ 전자레인지 살짝 데치기=비타민C 보존 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '브로콜리는 전자레인지로 살짝 데치면 비타민C 보존에 도움이 될 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '브로콜리'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '브로콜리는 전자레인지로 살짝 데치면 비타민C 보존에 도움이 될 수 있다.');

-- ㉘ 오래 삶으면 수용성 비타민 손실↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '브로콜리를 너무 오래 삶으면 수용성 비타민 손실이 커질 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '브로콜리'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '브로콜리를 너무 오래 삶으면 수용성 비타민 손실이 커질 수 있다.');


/* ===================== 🧄 마늘 ===================== */
-- ㉙ 알리신은 열에 전혀 파괴 안 됨 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '마늘의 알리신은 열에 전혀 파괴되지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '마늘'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '마늘의 알리신은 열에 전혀 파괴되지 않는다.');

-- ㉚ 다지거나 으깬 뒤 잠시 두면 알리신 생성 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '마늘은 다지거나 으깬 뒤 잠시 두면 알리나아제가 작동해 알리신이 생성된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '마늘'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '마늘은 다지거나 으깬 뒤 잠시 두면 알리나아제가 작동해 알리신이 생성된다.');


/* ===================== 🧅 양파 & 생강 & 버섯 ===================== */
-- ㉛ 양파 퀘르세틴=폴리페놀 항산화 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '양파의 퀘르세틴은 폴리페놀 계열의 항산화 물질이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '양파'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '양파의 퀘르세틴은 폴리페놀 계열의 항산화 물질이다.');

-- ㉜ 생강 진저롤·쇼가올 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '생강은 매운맛 성분 진저롤·쇼가올을 포함한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '생강'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '생강은 매운맛 성분 진저롤·쇼가올을 포함한다.');

-- ㉝ 버섯=비타민D 전구체, 햇볕에 말리면 ↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '버섯은 비타민D 전구체를 함유하며 햇볕에 말리면 증가할 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '버섯'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '버섯은 비타민D 전구체를 함유하며 햇볕에 말리면 증가할 수 있다.');


/* ===================== 🥩 소고기 ===================== */
-- ㉞ 철분·단백질 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '소고기는 철분과 단백질이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '소고기'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '소고기는 철분과 단백질이 풍부하다.');

-- ㉟ 냉동하면 영양소 절반 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '소고기는 냉동하면 영양소가 절반으로 줄어든다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '소고기'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '소고기는 냉동하면 영양소가 절반으로 줄어든다.');

-- ㊱ 익히면 단백질 파괴 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '소고기는 익히면 단백질이 파괴된다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '소고기'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '소고기는 익히면 단백질이 파괴된다.');

-- ㊲ 철·B12 거의 없다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '소고기에는 철과 비타민B12가 거의 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '소고기'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '소고기에는 철과 비타민B12가 거의 없다.');


/* ===================== 🐟 생선/해산물 ===================== */
-- ㊳ 연어=오메가-3 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '연어에는 오메가-3 지방산이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '연어'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '연어에는 오메가-3 지방산이 풍부하다.');

-- ㊴ 연어 냉동 시 오메가-3 소실 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '연어는 냉동하면 오메가-3가 사라진다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '연어'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '연어는 냉동하면 오메가-3가 사라진다.');

-- ㊵ 연어=비타민 D 공급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '연어는 비타민 D 공급원 중 하나다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '연어'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '연어는 비타민 D 공급원 중 하나다.');

-- ㊶ 고등어=오메가-3 대표 급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '고등어는 오메가-3 지방(DHA, EPA)의 대표적 급원이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '고등어'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '고등어는 오메가-3 지방(DHA, EPA)의 대표적 급원이다.');

-- ㊷ 고등어 통조림 뼈째=칼슘 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '고등어 통조림은 뼈째 먹으면 칼슘 섭취에 도움이 될 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '고등어'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '고등어 통조림은 뼈째 먹으면 칼슘 섭취에 도움이 될 수 있다.');

-- ㊸ 연어는 카로티노이드 전혀 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '연어는 붉은색이지만 카로티노이드를 전혀 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '연어'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '연어는 붉은색이지만 카로티노이드를 전혀 포함하지 않는다.');

-- ㊹ 참치=단백질 풍부·수은 노출 유의 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '참치는 단백질이 풍부하지만 수은 노출 가능성에 유의해야 한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '참치'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '참치는 단백질이 풍부하지만 수은 노출 가능성에 유의해야 한다.');

-- ㊺ 새우=콜레스테롤 있으나 단백질 급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '새우는 콜레스테롤 함량이 있으나 단백질 급원으로도 이용된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '새우'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '새우는 콜레스테롤 함량이 있으나 단백질 급원으로도 이용된다.');

-- ㊻ 오징어는 단백질이 적고 대부분 당질 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오징어는 단백질이 적고 대부분 당질로 구성된다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '오징어'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '오징어는 단백질이 적고 대부분 당질로 구성된다.');

-- ㊼ 문어=타우린 포함 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '문어는 타우린을 포함해 피로 회복에 도움을 줄 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '문어'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '문어는 타우린을 포함해 피로 회복에 도움을 줄 수 있다.');

-- ㊽ 굴=아연 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '굴은 아연 함량이 높아 면역에 도움이 될 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '굴'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '굴은 아연 함량이 높아 면역에 도움이 될 수 있다.');

-- ㊾ 조개류=철·B12 전혀 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '조개류는 철분과 비타민B12를 전혀 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '조개'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '조개류는 철분과 비타민B12를 전혀 포함하지 않는다.');

-- ㊿ 멸치=칼슘·단백질 급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '멸치는 칼슘과 단백질의 좋은 급원이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '멸치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '멸치는 칼슘과 단백질의 좋은 급원이다.');


/* ===================== 🥚·🥛 달걀 & 우유/유가공 ===================== */
-- 51 완전단백질 식품 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '계란은 완전단백질 식품이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '계란'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '계란은 완전단백질 식품이다.');

-- 52 노른자 콜레스테롤 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '계란 노른자에는 콜레스테롤이 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '계란'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '계란 노른자에는 콜레스테롤이 없다.');

-- 53 흰자=단백질 주성분 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '계란 흰자는 단백질이 주성분이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '계란'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '계란 흰자는 단백질이 주성분이다.');

-- 54 노른자=콜린 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '달걀 노른자는 콜린이 풍부해 두뇌 건강에 도움을 준다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '달걀'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '달걀 노른자는 콜린이 풍부해 두뇌 건강에 도움을 준다.');

-- 55 우유=칼슘·단백질 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '우유는 칼슘과 단백질이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '우유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '우유는 칼슘과 단백질이 풍부하다.');

-- 56 B2는 빛에 파괴 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '우유는 햇빛에 오래 노출되면 비타민 B2가 파괴된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '우유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '우유는 햇빛에 오래 노출되면 비타민 B2가 파괴된다.');

-- 57 냉장 보관 불필요 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '우유는 냉장 보관이 필요 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '우유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '우유는 냉장 보관이 필요 없다.');

-- 58 비타민D가 칼슘 흡수 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '우유의 칼슘 흡수를 위해 비타민D가 도움이 된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '우유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '우유의 칼슘 흡수를 위해 비타민D가 도움이 된다.');

-- 59 요거트=유익균 전혀 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '요거트는 발효식품이라 유익균을 전혀 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '요거트'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '요거트는 발효식품이라 유익균을 전혀 포함하지 않는다.');

-- 60 요거트 유산균, 가열에도 대부분 생존 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '요거트의 유산균은 가열 조리 시에도 대부분 살아남는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '요거트'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '요거트의 유산균은 가열 조리 시에도 대부분 살아남는다.');


/* ===================== 🍌🍓 과일 일반 ===================== */
-- 61 바나나 익을수록 전분→당 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '바나나는 익을수록 전분이 당으로 바뀐다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '바나나'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '바나나는 익을수록 전분이 당으로 바뀐다.');

-- 62 블루베리=안토시아닌 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '블루베리는 안토시아닌이 풍부한 과일로 알려져 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '블루베리'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '블루베리는 안토시아닌이 풍부한 과일로 알려져 있다.');

-- 63 아몬드=비타민E·불포화지방 거의 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '아몬드는 비타민E와 불포화지방을 거의 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '아몬드'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '아몬드는 비타민E와 불포화지방을 거의 포함하지 않는다.');

-- 64 꿀=100% 설탕과 동일, 미량영양소 0 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '꿀은 100% 설탕과 동일해 미량영양소가 전혀 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '꿀'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '꿀은 100% 설탕과 동일해 미량영양소가 전혀 없다.');

-- 65 레몬=비타민C 풍부·철 흡수 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '레몬은 비타민C가 풍부하며 철 흡수에 도움을 줄 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '레몬'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '레몬은 비타민C가 풍부하며 철 흡수에 도움을 줄 수 있다.');

-- 66 오렌지=비타민C 거의 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '오렌지는 감귤류지만 비타민C를 거의 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '오렌지'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '오렌지는 감귤류지만 비타민C를 거의 포함하지 않는다.');

-- 67 포도 껍질 레스베라트롤=폴리페놀 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '포도 껍질의 레스베라트롤은 폴리페놀의 일종이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '포도'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '포도 껍질의 레스베라트롤은 폴리페놀의 일종이다.');

-- 68 배=펙틴 전혀 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '배는 식이섬유인 펙틴을 전혀 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '배'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '배는 식이섬유인 펙틴을 전혀 포함하지 않는다.');

-- 69 복숭아=수분·비타민A 전구체 포함 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '복숭아는 수분과 비타민A 전구체를 포함한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '복숭아'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '복숭아는 수분과 비타민A 전구체를 포함한다.');

-- 70 딸기=비타민C가 항상 레몬보다 낮다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '딸기는 비타민C가 적어 레몬보다 항상 낮다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '딸기'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '딸기는 비타민C가 적어 레몬보다 항상 낮다.');

-- 71 키위=비타민C·식이섬유 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '키위는 비타민C와 식이섬유가 풍부한 과일이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '키위'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '키위는 비타민C와 식이섬유가 풍부한 과일이다.');

-- 72 파인애플 브로멜라인=단백질 분해효소 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '파인애플의 브로멜라인은 단백질 분해효소이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '파인애플'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '파인애플의 브로멜라인은 단백질 분해효소이다.');

-- 73 망고=베타카로틴 거의 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '망고는 베타카로틴을 거의 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '망고'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '망고는 베타카로틴을 거의 포함하지 않는다.');

-- 74 아보카도=불포화지방·칼륨 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '아보카도는 불포화지방과 칼륨이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '아보카도'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '아보카도는 불포화지방과 칼륨이 풍부하다.');


/* ===================== 🫒 식용유/지방 ===================== */
-- 75 올리브오일 주 지방산=올레산 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '올리브오일의 주된 지방산은 올레산(단일불포화지방)이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '올리브오일'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '올리브오일의 주된 지방산은 올레산(단일불포화지방)이다.');

-- 76 카놀라유=불포화지방 비율 높음 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '카놀라유는 불포화지방 비율이 높은 편이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '카놀라유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '카놀라유는 불포화지방 비율이 높은 편이다.');

-- 77 해바라기유=비타민E 거의 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '해바라기유는 비타민E를 거의 포함하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '해바라기유'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '해바라기유는 비타민E를 거의 포함하지 않는다.');

-- 78 코코넛오일=중쇄지방만, 칼로리 0 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '코코넛오일은 주로 중쇄지방산으로만 이루어져 칼로리가 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '코코넛오일'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '코코넛오일은 주로 중쇄지방산으로만 이루어져 칼로리가 없다.');

-- 79 들깨=오메가-3(ALA) 높음 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '들깨는 오메가-3 지방산(알파-리놀렌산) 함량이 높은 편이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '들깨'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '들깨는 오메가-3 지방산(알파-리놀렌산) 함량이 높은 편이다.');

-- 80 들기름=산패 잘 안 되고 고온 튀김 적합 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '들기름은 산패가 잘 되지 않아 언제나 고온 튀김에 적합하다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '들기름'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '들기름은 산패가 잘 되지 않아 언제나 고온 튀김에 적합하다.');

-- 81 참기름=향 강해 마무리용 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '참기름은 향이 강하고 주로 마무리용으로 사용된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '참기름'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '참기름은 향이 강하고 주로 마무리용으로 사용된다.');

-- 82 버터=주로 불포화지방으로만 구성 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '버터는 주로 불포화지방으로만 구성된다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '버터'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '버터는 주로 불포화지방으로만 구성된다.');


/* ===================== 🫘 콩/두류 & 곡물 ===================== */
-- 83 두부=식물성 단백질·칼슘 공급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '두부는 식물성 단백질과 칼슘의 공급원이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '두부'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '두부는 식물성 단백질과 칼슘의 공급원이다.');

-- 84 현미=식이섬유가 백미보다 많다 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '현미는 도정하지 않은 곡물이라 식이섬유가 백미보다 많다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '현미'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '현미는 도정하지 않은 곡물이라 식이섬유가 백미보다 많다.');

-- 85 귀리=베타글루칸 급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '귀리는 베타글루칸 식이섬유의 좋은 급원이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '귀리'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '귀리는 베타글루칸 식이섬유의 좋은 급원이다.');

-- 86 퀴노아=완전단백질에 가까운 아미노산 구성 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '퀴노아는 완전단백질에 가까운 아미노산 구성을 가진다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '퀴노아'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '퀴노아는 완전단백질에 가까운 아미노산 구성을 가진다.');

-- 87 고구마=식이섬유·베타카로틴 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '고구마는 식이섬유와 베타카로틴이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '고구마'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '고구마는 식이섬유와 베타카로틴이 풍부하다.');

-- 88 보리=베타글루칸 포함 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '보리는 베타글루칸을 포함하는 곡물이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '보리'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '보리는 베타글루칸을 포함하는 곡물이다.');

-- 89 콩=식물성 단백질·이소플라본 급원 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '콩은 식물성 단백질과 이소플라본의 급원이다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '콩'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '콩은 식물성 단백질과 이소플라본의 급원이다.');

-- 90 렌틸콩=철·식이섬유 적어 영양가 낮다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '렌틸콩은 철분과 식이섬유가 적어 영양가가 낮다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '렌틸콩'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '렌틸콩은 철분과 식이섬유가 적어 영양가가 낮다.');

-- 91 병아리콩=단백질·섬유소 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '병아리콩은 후무스의 주재료로 단백질과 섬유소가 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '병아리콩'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '병아리콩은 후무스의 주재료로 단백질과 섬유소가 풍부하다.');

-- 92 율무=글루텐 풍부한 밀과 동일 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '율무는 글루텐이 풍부한 밀과 동일한 곡물이다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '율무'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '율무는 글루텐이 풍부한 밀과 동일한 곡물이다.');

-- 93 현미=백미보다 항상 비소 낮지 않다 (※원문 F 문장 주의)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '현미는 백미보다 항상 비소(아르센) 함량이 높은 것은 아니다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '현미'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '현미는 백미보다 항상 비소(아르센) 함량이 높은 것은 아니다.');


/* ===================== 🥜 견과/씨앗 ===================== */
-- 94 브라질너트=셀레늄 높아 과다 주의 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '브라질너트는 셀레늄 함량이 높아 과다 섭취에 주의가 필요하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '브라질너트'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '브라질너트는 셀레늄 함량이 높아 과다 섭취에 주의가 필요하다.');

-- 95 피스타치오=단백질·식이섬유 모두 낮다 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '피스타치오는 단백질과 식이섬유가 모두 낮다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '피스타치오'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '피스타치오는 단백질과 식이섬유가 모두 낮다.');

-- 96 호두=오메가-3(ALA) 포함 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '호두는 오메가-3(ALA)을 포함하는 견과류다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '호두'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '호두는 오메가-3(ALA)을 포함하는 견과류다.');

-- 97 캐슈넛=불포화지방·마그네슘 일부 제공 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '캐슈넛은 불포화지방과 마그네슘을 일부 제공한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '캐슈넛'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '캐슈넛은 불포화지방과 마그네슘을 일부 제공한다.');

-- 98 참깨=칼슘·리그난 포함 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '참깨는 칼슘과 리그난 성분을 포함한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '참깨'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '참깨는 칼슘과 리그난 성분을 포함한다.');

-- 99 검은깨=참깨와 영양 유사 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '검은깨는 참깨와 영양이 완전히 동일하지는 않지만 유사하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '검은깨'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '검은깨는 참깨와 영양이 완전히 동일하지는 않지만 유사하다.');

-- 100 치아씨드=수분 만나 젤 형성 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '치아씨드는 수분을 만나 젤 형태로 변한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '치아씨드'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '치아씨드는 수분을 만나 젤 형태로 변한다.');

-- 101 아마씨=ALA 급원, 통째로 먹으면 흡수↓ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '아마씨는 오메가-3(ALA)의 급원이지만 통째로 먹으면 흡수가 떨어질 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '아마씨'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '아마씨는 오메가-3(ALA)의 급원이지만 통째로 먹으면 흡수가 떨어질 수 있다.');

-- 102 카카오닙스=식이섬유·폴리페놀 풍부 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '카카오닙스는 식이섬유와 폴리페놀이 풍부하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '카카오닙스'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '카카오닙스는 식이섬유와 폴리페놀이 풍부하다.');


/* ===================== 🧂 조미료/발효 ===================== */
-- 103 된장=나트륨 없어서 제한 불필요 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '된장은 발효식품이지만 나트륨이 없어서 아무 제한이 필요 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '된장'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '된장은 발효식품이지만 나트륨이 없어서 아무 제한이 필요 없다.');

-- 104 고추장=캡사이신 매운맛 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '고추장은 캡사이신으로 매운맛을 낸다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '고추장'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '고추장은 캡사이신으로 매운맛을 낸다.');

-- 105 간장=나트륨 높을 수 있어 주의 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '간장은 나트륨 함량이 높을 수 있어 과다 섭취에 주의해야 한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '간장'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '간장은 나트륨 함량이 높을 수 있어 과다 섭취에 주의해야 한다.');

-- 106 소금=나트륨 주요 원천 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '소금은 나트륨 섭취의 주요 원천 중 하나다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '소금'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '소금은 나트륨 섭취의 주요 원천 중 하나다.');

-- 107 설탕=열량 0, 대사 영향 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '설탕은 열량이 없고 대사에 영향을 주지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '설탕'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '설탕은 열량이 없고 대사에 영향을 주지 않는다.');

-- 108 후추 피페린=향·매운맛 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '후추의 피페린은 향과 매운맛을 낸다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '후추'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '후추의 피페린은 향과 매운맛을 낸다.');

-- 109 고춧가루 캡사이신=지용성, 기름과 먹으면 퍼짐 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '고춧가루의 캡사이신은 지용성이어서 기름과 함께 먹으면 매운맛이 더 퍼질 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '고춧가루'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '고춧가루의 캡사이신은 지용성이어서 기름과 함께 먹으면 매운맛이 더 퍼질 수 있다.');

-- 110 카레의 커큐민=흡수 낮아 후추·기름 도움 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '카레의 강황(커큐민)은 흡수율이 낮아 후추와 기름이 도움이 될 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '카레'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '카레의 강황(커큐민)은 흡수율이 낮아 후추와 기름이 도움이 될 수 있다.');

-- 111 계피=혈당 조절 도움 성분 전혀 없음 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '계피는 혈당 조절에 도움을 주는 성분이 전혀 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '계피'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '계피는 혈당 조절에 도움을 주는 성분이 전혀 없다.');

-- 112 김치 B군 전혀 생성 안 됨 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '김치는 발효 과정에서 비타민B군이 전혀 생기지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '김치'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '김치는 발효 과정에서 비타민B군이 전혀 생기지 않는다.');

-- 113 김치 젖산균=냉장 보관이 발효 늦춤 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '김치의 젖산균은 냉장 보관이 발효를 늦추는 데 도움이 된다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '김치'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '김치의 젖산균은 냉장 보관이 발효를 늦추는 데 도움이 된다.');


/* ===================== 🥖 빵/초콜릿/음료 ===================== */
-- 114 통밀빵=정제빵보다 섬유↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '통밀빵은 정제 밀가루 빵보다 식이섬유가 많다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '빵'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '통밀빵은 정제 밀가루 빵보다 식이섬유가 많다.');

-- 115 다크초콜릿=카카오↑ → 폴리페놀↑ (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '다크초콜릿은 카카오 함량이 높을수록 폴리페놀 함량이 높다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '초콜릿'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '다크초콜릿은 카카오 함량이 높을수록 폴리페놀 함량이 높다.');

-- 116 커피 카페인=항산화와 함께 절대 존재 X (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '커피의 카페인은 절대 항산화 물질과 함께 존재하지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '커피'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '커피의 카페인은 절대 항산화 물질과 함께 존재하지 않는다.');

-- 117 녹차 카테킨=항산화 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '녹차의 카테킨은 항산화 작용으로 알려져 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '녹차'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '녹차의 카테킨은 항산화 작용으로 알려져 있다.');

-- 118 홍차=발효라 카페인 0 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '홍차는 발효 과정 때문에 카페인이 전혀 없다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '홍차'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '홍차는 발효 과정 때문에 카페인이 전혀 없다.');

-- 119 물=체온조절·대사에 중요 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '물을 충분히 마시는 것은 체온 조절과 대사에 중요하다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '물'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '물을 충분히 마시는 것은 체온 조절과 대사에 중요하다.');

-- 120 탄산음료=첨가당 많아 과다 주의 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '탄산음료는 대부분 첨가당이 많아 과다 섭취에 유의해야 한다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '탄산음료'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '탄산음료는 대부분 첨가당이 많아 과다 섭취에 유의해야 한다.');

-- 121 에너지드링크=카페인·당 낮아 수분보충만 적합 (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '에너지드링크는 카페인과 당류가 낮아 수분보충에만 적합하다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '에너지드링크'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '에너지드링크는 카페인과 당류가 낮아 수분보충에만 적합하다.');

-- 122 주스=100%라도 통과일보다 식이섬유 적다 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '주스는 과일 100%라도 식이섬유는 통과일보다 적다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '주스'
  AND NOT EXISTS (SELECT 1 FROM quiz_pool qp WHERE qp.statement = '주스는 과일 100%라도 식이섬유는 통과일보다 적다.');

-- 123 식초=위산·소화에 전혀 영향 X (F)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '식초는 산성이라서 위산 분비와 소화에 전혀 영향을 주지 않는다.', FALSE, TRUE
FROM ingredient i
WHERE i.name = '식초'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '식초는 산성이라서 위산 분비와 소화에 전혀 영향을 주지 않는다.');


/* ===================== 🫘 두유 & 치즈 ===================== */
-- 124 두유=유당 없어 불내증 대안 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '두유는 유당이 없어 유당불내증인 사람에게 대안이 될 수 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '두유'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '두유는 유당이 없어 유당불내증인 사람에게 대안이 될 수 있다.');

-- 125 치즈=칼슘 풍부, 나트륨 확인 필요 (T)
INSERT INTO quiz_pool (ingredient_id, statement, answer, is_active)
SELECT i.id, '치즈는 칼슘이 풍부하지만 나트륨 함량을 확인할 필요가 있다.', TRUE, TRUE
FROM ingredient i
WHERE i.name = '치즈'
  AND NOT EXISTS (SELECT 1
                  FROM quiz_pool qp
                  WHERE qp.statement = '치즈는 칼슘이 풍부하지만 나트륨 함량을 확인할 필요가 있다.');