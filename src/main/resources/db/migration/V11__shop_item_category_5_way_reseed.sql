-- 상점 카테고리 개편: 4종 여행소품(GLASSES/BAG/CARRIER/HAT) → 5종 의류 컨셉(TOP/BOTTOM/HAT/SHOES/ACCESSORY)
-- (이슈 #8) FE Figma가 "옷 갈아입히기" 5탭 구조로 설계돼 있었는데 백엔드는 여행소품 4종만 갖고 있어
-- top/bottom/shoes 탭이 항상 빈 상태였다. 기존 GLASSES/BAG/CARRIER는 ACCESSORY로 합치고,
-- top/bottom/shoes용 아이템 9개를 새로 추가한다.
-- 아직 실사용자가 없는 개발 단계라 V9와 동일하게 기존 UserItem 참조 걱정 없이 재시딩한다.
--
-- items.category/user_items.category는 @Enumerated(STRING) 필드라 Hibernate가 ddl-auto: update로
-- 테이블을 처음 만들 때 CHECK 제약(옛 4종 값)을 같이 생성해뒀다. Flyway는 Hibernate의 스키마 갱신보다
-- 먼저 실행되므로, 이 마이그레이션에서 옛 제약을 미리 지워야 새 카테고리 값을 넣을 수 있다.
-- (제약 자체는 이후 Hibernate ddl-auto: update가 현재 엔티티 기준으로 다시 만들어준다.)

ALTER TABLE items DROP CONSTRAINT IF EXISTS items_category_check;
ALTER TABLE user_items DROP CONSTRAINT IF EXISTS user_items_category_check;

DELETE FROM user_items;
DELETE FROM items;

INSERT INTO items (name, description, price, image_url, category, created_at, updated_at) VALUES
    -- TOP
    ('그린 라운드넥 티셔츠', '산뜻한 그린 라운드넥 티셔츠', 350, NULL, 'TOP', now(), now()),
    ('스트라이프 셔츠', '캐주얼한 스트라이프 셔츠', 450, NULL, 'TOP', now(), now()),
    ('옐로우 후드티', '포근한 옐로우 후드티', 550, NULL, 'TOP', now(), now()),
    ('블루 후리스 자켓', '따뜻한 블루 후리스 자켓', 700, NULL, 'TOP', now(), now()),
    ('그린 조끼', '실용적인 그린 조끼', 650, NULL, 'TOP', now(), now()),
    -- BOTTOM
    ('데님 반바지', '활동적인 데님 반바지', 350, NULL, 'BOTTOM', now(), now()),
    ('카고 팬츠', '튼튼한 카고 팬츠', 500, NULL, 'BOTTOM', now(), now()),
    -- HAT
    ('밀짚모자', '여름 여행 필수 밀짚모자', 300, NULL, 'HAT', now(), now()),
    ('등산 모자', '자외선 차단 등산 모자', 400, NULL, 'HAT', now(), now()),
    -- SHOES
    ('캔버스 스니커즈', '편안한 캔버스 스니커즈', 550, NULL, 'SHOES', now(), now()),
    ('샌들', '시원한 여름용 샌들', 400, NULL, 'SHOES', now(), now()),
    -- ACCESSORY (기존 여행소품: 선글라스/배낭/캐리어)
    ('여행자 선글라스', '세련된 여행자 선글라스', 500, NULL, 'ACCESSORY', now(), now()),
    ('파일럿 선글라스', '멋진 파일럿 스타일 선글라스', 800, NULL, 'ACCESSORY', now(), now()),
    ('등산 배낭', '튼튼한 등산용 배낭', 600, NULL, 'ACCESSORY', now(), now()),
    ('여행용 크로스백', '가벼운 여행용 크로스백', 400, NULL, 'ACCESSORY', now(), now()),
    ('캐리어 20인치', '여행 필수품 캐리어', 1000, NULL, 'ACCESSORY', now(), now()),
    ('캐리어 24인치', '넉넉한 장기 여행용 캐리어', 1200, NULL, 'ACCESSORY', now(), now());
