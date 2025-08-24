-- 값 변경 시 파일 내용이 바뀌고, Flyway가 체크섬 변화 감지 → 재실행

UPDATE ingredient
SET icon_url = '🍅'
WHERE name = '토마토';
UPDATE ingredient
SET icon_url = '🥒'
WHERE name = '오이';
UPDATE ingredient
SET icon_url = '🥔'
WHERE name = '감자';
UPDATE ingredient
SET icon_url = '🧄'
WHERE name = '마늘';
UPDATE ingredient
SET icon_url = '🧅'
WHERE name = '양파';
UPDATE ingredient
SET icon_url = '🍌'
WHERE name = '바나나';
UPDATE ingredient
SET icon_url = '🍯'
WHERE name = '꿀';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '소금';
UPDATE ingredient
SET icon_url = '🌿'
WHERE name = '파슬리';
UPDATE ingredient
SET icon_url = '🫑'
WHERE name = '파프리카';
UPDATE ingredient
SET icon_url = '🥬'
WHERE name = '시금치';
UPDATE ingredient
SET icon_url = '🧈'
WHERE name = '버터';
UPDATE ingredient
SET icon_url = '🧴'
WHERE name = '식초';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '베이킹소다';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '베이킹파우더';
UPDATE ingredient
SET icon_url = '🫙'
WHERE name = '들기름';
UPDATE ingredient
SET icon_url = '🫙'
WHERE name = '참기름';
UPDATE ingredient
SET icon_url = '🫙'
WHERE name = '식용유';
UPDATE ingredient
SET icon_url = '🍠'
WHERE name = '고구마';
UPDATE ingredient
SET icon_url = '🥦'
WHERE name = '브로콜리';
UPDATE ingredient
SET icon_url = '🍗'
WHERE name = '닭고기';
UPDATE ingredient
SET icon_url = '🐟'
WHERE name = '연어';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '쌀';
UPDATE ingredient
SET icon_url = '🍄'
WHERE name = '버섯';
UPDATE ingredient
SET icon_url = '🥩'
WHERE name = '쇠고기';
UPDATE ingredient
SET icon_url = '🍝'
WHERE name = '파스타';
UPDATE ingredient
SET icon_url = '🥚'
WHERE name = '달걀';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '검은깨';
UPDATE ingredient
SET icon_url = '🧀'
WHERE name = '치즈';
UPDATE ingredient
SET icon_url = '🥛'
WHERE name = '요거트';
UPDATE ingredient
SET icon_url = '🐟'
WHERE name = '고등어';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '현미';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '렌틸콩';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '퀴노아';
UPDATE ingredient
SET icon_url = '🧊'
WHERE name = '냉동식품';
UPDATE ingredient
SET icon_url = '🫒'
WHERE name = '올리브유';
UPDATE ingredient
SET icon_url = '🍬'
WHERE name = '설탕';
UPDATE ingredient
SET icon_url = '🍋'
WHERE name = '레몬';
UPDATE ingredient
SET icon_url = '🍎'
WHERE name = '사과';
UPDATE ingredient
SET icon_url = '🍍'
WHERE name = '파인애플';
UPDATE ingredient
SET icon_url = '🥝'
WHERE name = '키위';
UPDATE ingredient
SET icon_url = '🌱'
WHERE name = '콩나물';
UPDATE ingredient
SET icon_url = '🐟'
WHERE name = '꽁치';
UPDATE ingredient
SET icon_url = '🍞'
WHERE name = '통곡물빵';
UPDATE ingredient
SET icon_url = '🫚'
WHERE name = '생강';
UPDATE ingredient
SET icon_url = '🌿'
WHERE name = '바질';
UPDATE ingredient
SET icon_url = '🍫'
WHERE name = '카카오';
UPDATE ingredient
SET icon_url = '🍫'
WHERE name = '초콜릿';
UPDATE ingredient
SET icon_url = '🍄'
WHERE name = '표고버섯';
UPDATE ingredient
SET icon_url = '🐟'
WHERE name = '멸치';
UPDATE ingredient
SET icon_url = '🐟'
WHERE name = '참치';
UPDATE ingredient
SET icon_url = '🥛'
WHERE name = '두유';
UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '아몬드';
UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '땅콩';
UPDATE ingredient
SET icon_url = '🌽'
WHERE name = '옥수수';
UPDATE ingredient
SET icon_url = '🌶️'
WHERE name = '고추';
UPDATE ingredient
SET icon_url = '🥥'
WHERE name = '코코넛오일';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '라면스프';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '메밀';
UPDATE ingredient
SET icon_url = '🥛'
WHERE name = '우유';
UPDATE ingredient
SET icon_url = '🧈'
WHERE name = '마가린';
UPDATE ingredient
SET icon_url = '🥫'
WHERE name = '장류';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '쌀가루';

-- 해제 대상 4종(미역/대파/다시마/곶감)은 퀴즈에서 제외하지만, 아이콘은 임시값으로 채워둠
UPDATE ingredient
SET icon_url = '🌿'
WHERE name = '대파';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '두부';
UPDATE ingredient
SET icon_url = '🍙'
WHERE name = '김';
UPDATE ingredient
SET icon_url = '🪸'
WHERE name = '미역';
UPDATE ingredient
SET icon_url = '🥫'
WHERE name = '된장';
UPDATE ingredient
SET icon_url = '🪸'
WHERE name = '다시마';
UPDATE ingredient
SET icon_url = '🍊'
WHERE name = '곶감';
UPDATE ingredient
SET icon_url = '🥕'
WHERE name = '당근';

UPDATE ingredient
SET icon_url = '🥩'
WHERE name = '소고기';
UPDATE ingredient
SET icon_url = '🦐'
WHERE name = '새우';
UPDATE ingredient
SET icon_url = '🦑'
WHERE name = '오징어';
UPDATE ingredient
SET icon_url = '🐙'
WHERE name = '문어';
UPDATE ingredient
SET icon_url = '🦪'
WHERE name = '굴';
UPDATE ingredient
SET icon_url = '🐚'
WHERE name = '조개';

UPDATE ingredient
SET icon_url = '🥚'
WHERE name = '계란';
UPDATE ingredient
SET icon_url = '🫐'
WHERE name = '블루베리';
UPDATE ingredient
SET icon_url = '🍊'
WHERE name = '오렌지';
UPDATE ingredient
SET icon_url = '🍇'
WHERE name = '포도';
UPDATE ingredient
SET icon_url = '🍐'
WHERE name = '배';
UPDATE ingredient
SET icon_url = '🍑'
WHERE name = '복숭아';
UPDATE ingredient
SET icon_url = '🍓'
WHERE name = '딸기';
UPDATE ingredient
SET icon_url = '🥭'
WHERE name = '망고';
UPDATE ingredient
SET icon_url = '🥑'
WHERE name = '아보카도';

UPDATE ingredient
SET icon_url = '🫒'
WHERE name = '올리브오일';
UPDATE ingredient
SET icon_url = '🫙'
WHERE name = '카놀라유';
UPDATE ingredient
SET icon_url = '🌻'
WHERE name = '해바라기유';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '들깨';

UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '귀리';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '보리';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '콩';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '병아리콩';
UPDATE ingredient
SET icon_url = '🌾'
WHERE name = '율무';

UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '브라질너트';
UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '피스타치오';
UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '호두';
UPDATE ingredient
SET icon_url = '🥜'
WHERE name = '캐슈넛';

UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '참깨';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '치아씨드';
UPDATE ingredient
SET icon_url = '🫘'
WHERE name = '아마씨';
UPDATE ingredient
SET icon_url = '🍫'
WHERE name = '카카오닙스';

UPDATE ingredient
SET icon_url = '🥫'
WHERE name = '고추장';
UPDATE ingredient
SET icon_url = '🫙'
WHERE name = '간장';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '후추';
UPDATE ingredient
SET icon_url = '🌶️'
WHERE name = '고춧가루';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '카레';
UPDATE ingredient
SET icon_url = '🧂'
WHERE name = '계피';
UPDATE ingredient
SET icon_url = '🥬'
WHERE name = '김치';

UPDATE ingredient
SET icon_url = '🍞'
WHERE name = '빵';
UPDATE ingredient
SET icon_url = '☕'
WHERE name = '커피';
UPDATE ingredient
SET icon_url = '🍵'
WHERE name = '녹차';
UPDATE ingredient
SET icon_url = '🫖'
WHERE name = '홍차';
UPDATE ingredient
SET icon_url = '💧'
WHERE name = '물';
UPDATE ingredient
SET icon_url = '🥤'
WHERE name = '탄산음료';
UPDATE ingredient
SET icon_url = '🥤'
WHERE name = '에너지드링크';
UPDATE ingredient
SET icon_url = '🧃'
WHERE name = '주스';