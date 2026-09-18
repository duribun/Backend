-- docs/ISSUE-지역마스코트시드-실데이터교체.md — V2가 심어둔 서울/강릉 2곳짜리 테스트 데이터를 Figma
-- "(3) 위치 인증" 플로우(node 312:133) 기준 실제 기획 데이터 10개 지역으로 교체한다.
--
-- RegionSeedConfig/MascotSeedConfig(dev 프로필 전용 CommandLineRunner)는 이미 10개로 갱신했지만,
-- 두 시더 모두 "테이블이 비어있을 때만" 시드하는 멱등 가드(count() > 0 이면 return)라, V2가
-- 이미 심어둔 2개 행이 있는 한 절대 재시딩되지 않는다 — V2 주석에 "prod가 dev 시더와 같은
-- baseline 데이터를 갖게 하려고 SQL로도 미러링한다"고 적혀있는데, 그 SQL 쪽을 갱신하지 않고
-- Java 시더만 고쳐서는 dev/prod 어느 환경에서도 실제로 반영되지 않는 문제가 있었다.
--
-- regions/mascots엔 DB 레벨 FK 제약이 없어서(V1 baseline 기준 region_id는 그냥 bigint 컬럼) 순서
-- 상관없이 안전하게 지울 수 있다. 기존 강릉시/서울특별시(id 1,2)를 참조하던 visit_records가
-- 있다면 region_id가 고아 값이 되지만, 개발 단계 테스트 데이터라 문제 없다고 판단했다.
--
-- sigunguCode/좌표는 RegionSeedConfig.java와 동일하게 초안이다 — 정식 반영 전
-- 행정표준코드관리시스템(code.go.kr) 및 실좌표 재검증 필요(해당 파일 주석 참고).
DELETE FROM mascots;
DELETE FROM regions;

INSERT INTO regions (name, sigungu_code, latitude, longitude, verification_radius_meters, created_at, updated_at) VALUES
    ('독도', '47431', 37.2416, 131.8631, 1000, now(), now()),
    ('서울특별시', '11000', 37.5665, 126.9780, 1000, now(), now()),
    ('울산광역시', '31000', 35.5384, 129.3114, 1000, now(), now()),
    ('울릉군', '47430', 37.4845, 130.9057, 1000, now(), now()),
    ('부산광역시', '26000', 35.1796, 129.0756, 1000, now(), now()),
    ('대전광역시', '30000', 36.3504, 127.3845, 1000, now(), now()),
    ('대구광역시', '27000', 35.8714, 128.6014, 1000, now(), now()),
    ('광주광역시', '29000', 35.1595, 126.8526, 1000, now(), now()),
    ('인천광역시', '28000', 37.4563, 126.7052, 1000, now(), now()),
    ('제주특별자치도', '50000', 33.4996, 126.5312, 1000, now(), now());

INSERT INTO mascots (region_id, name, description, image_url, created_at, updated_at) VALUES
    ((SELECT id FROM regions WHERE sigungu_code = '47431'), '강치', '독도의 경비대 강치! 우리를 안전하게 지켜줘요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '11000'), '해치', '서울의 수호자 해치! 언제나 씩씩하게 서울을 지켜줘요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '31000'), '고래', '울산 앞바다의 고래! 시원한 바다 여행을 함께해요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '47430'), '괭이갈매기', '섬과 바다의 친구 괭이갈매기! 울릉도 곳곳을 자유롭게 날아다녀요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '26000'), '동백', '차가운 겨울에 피는 부산의 동백! 따뜻한 마음을 전해줘요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '30000'), '빵', '빵의 도시 대전! 고소한 향기로 여행자를 이끌어요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '27000'), '사과', '햇살 가득 머금은 대구 사과! 상큼한 맛으로 여행에 활력을 더해줘요', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '29000'), '철쭉', '봄을 알리는 무등산의 철쭉! 멋진 꽃길로 여행자를 반겨줘요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '28000'), '학', '인천의 자연을 품은 학! 갯벌과 바다를 자유롭게 여행해요.', NULL, now(), now()),
    ((SELECT id FROM regions WHERE sigungu_code = '50000'), '노루', '제주 숲속의 귀여운 노루! 자연과 함께 즐거운 여행을 떠나요.', NULL, now(), now());
