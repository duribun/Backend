-- 상점 아이템을 Figma "(6) 상점" 프레임 기준으로 재시딩하고 모자/악세서리를 제거 (duribun/Backend#70)
-- Figma 그룹명/가격을 카테고리별로 대조한 결과:
--  - TOP: 기존 이름/가격이 실제 Figma 레이어명과 달랐음(예: "그린 라운드넥 티셔츠" -> 실제는 "초록 상의") -> 이름/가격 정정.
--  - BOTTOM/SHOES: Figma에 상의보다 더 많은 종류(5/6종)가 그려져 있었음 -> 개수까지 Figma에 맞춰 전체 교체.
--  - HAT/ACCESSORY: 아직 디자인 자체가 없어 목업이 빈 상태 -> imageUrl null인 채로 8개를 남겨두는 대신
--    FE가 "아직 아이템이 없어요" 빈 상태를 그대로 보여주도록 시드에서 제외(ItemCategory enum 값은 유지 —
--    추후 디자인이 나오면 재사용).
-- 아직 실사용자가 없는 개발 단계라 V9/V11과 동일하게 기존 UserItem 참조 걱정 없이 재시딩한다.

DELETE FROM user_items;
DELETE FROM items;

INSERT INTO items (name, description, price, image_url, category, created_at, updated_at) VALUES
    -- TOP (Figma 그룹명/가격 그대로 반영 — 기존 이름은 실제 레이어명과 달랐음)
    ('초록 상의', '산뜻한 초록 상의', 30, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/1.png', 'TOP', now(), now()),
    ('파란 셔츠', '캐주얼한 파란 셔츠', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/2.png', 'TOP', now(), now()),
    ('노란 상의', '포근한 노란 상의', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/3.png', 'TOP', now(), now()),
    ('파란 후리스', '따뜻한 파란 후리스', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/4.png', 'TOP', now(), now()),
    ('초록 후리스', '포근한 초록 후리스', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/5.png', 'TOP', now(), now()),
    -- BOTTOM
    ('파란 반바지', '시원한 파란 반바지', 30, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/bottom-blue-shorts.png', 'BOTTOM', now(), now()),
    ('초록 반바지', '산뜻한 초록 반바지', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/bottom-green-shorts.png', 'BOTTOM', now(), now()),
    ('노란 반바지', '발랄한 노란 반바지', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/bottom-yellow-shorts.png', 'BOTTOM', now(), now()),
    ('파란 긴바지', '캐주얼한 파란 긴바지', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/bottom-blue-long-pants.png', 'BOTTOM', now(), now()),
    ('초록 긴바지', '편안한 초록 긴바지', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/bottom-green-long-pants.png', 'BOTTOM', now(), now()),
    -- SHOES
    ('초록 신발', '발랄한 초록 신발', 30, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-green.png', 'SHOES', now(), now()),
    ('갈색 신발', '클래식한 갈색 신발', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-brown-sneaker.png', 'SHOES', now(), now()),
    ('노란 신발', '포인트가 되는 노란 신발', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-yellow.png', 'SHOES', now(), now()),
    ('흰색 신발', '깔끔한 흰색 신발', 40, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-white.png', 'SHOES', now(), now()),
    ('등산 신발', '험한 길도 거뜬한 등산 신발', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-hiking-boots.png', 'SHOES', now(), now()),
    ('초록 샌들 신발', '시원한 초록 샌들', 70, 'https://duribun-s3-bucket-678191059624-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/items/shoes-green-sandal.png', 'SHOES', now(), now());
