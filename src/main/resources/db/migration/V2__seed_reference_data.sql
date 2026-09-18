-- Reference data mirroring the dev-only @Profile("dev") seeders (RegionSeedConfig,
-- CharacterSeedConfig, BadgeSeedConfig, ItemSeedConfig), so prod ships with the same
-- baseline data. Region scope intentionally limited to the same test subset as those
-- seeders (see docs/map.md) — full sigungu coverage is out of scope for this migration.

INSERT INTO regions (name, sigungu_code, latitude, longitude, verification_radius_meters, created_at, updated_at) VALUES
    ('강릉시', '51150', 37.7519, 128.8761, 1000, now(), now()),
    ('서울특별시', '11000', 37.5665, 126.9780, 1000, now(), now());

INSERT INTO characters (region_id, name, description, image_url, created_at, updated_at) VALUES
    ((SELECT id FROM regions WHERE sigungu_code = '51150'), '강릉이', '강릉을 사랑하는 캐릭터', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '11000'), '서울이', '서울을 사랑하는 캐릭터', NULL, now(), now());

INSERT INTO badges (code, name, description, required_visit_count, icon_url, created_at, updated_at) VALUES
    ('BEGINNER', '여행 초보자', '지역 1곳을 방문 인증했어요', 1, NULL, now(), now()),
    ('EXPLORER', '국내 탐험가', '지역 5곳을 방문 인증했어요', 5, NULL, now(), now()),
    ('MASTER', '여행 마스터', '지역 10곳을 방문 인증했어요', 10, NULL, now(), now());

INSERT INTO items (name, description, price, image_url, category, created_at, updated_at) VALUES
    ('여행자 선글라스', '세련된 여행자 선글라스', 500, NULL, 'GLASSES', now(), now()),
    ('파일럿 선글라스', '멋진 파일럿 스타일 선글라스', 800, NULL, 'GLASSES', now(), now()),
    ('등산 배낭', '튼튼한 등산용 배낭', 600, NULL, 'BAG', now(), now()),
    ('여행용 크로스백', '가벼운 여행용 크로스백', 400, NULL, 'BAG', now(), now()),
    ('캐리어 20인치', '여행 필수품 캐리어', 1000, NULL, 'CARRIER', now(), now()),
    ('캐리어 24인치', '넉넉한 장기 여행용 캐리어', 1200, NULL, 'CARRIER', now(), now()),
    ('밀짚모자', '여름 여행 필수 밀짚모자', 300, NULL, 'HAT', now(), now()),
    ('등산 모자', '자외선 차단 등산 모자', 400, NULL, 'HAT', now(), now());
