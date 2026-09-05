-- docs/badge.md: Badge 도메인이 "방문 수" 대신 "마스코트 획득 수"를 기준으로 삼도록 확정
-- (LocationVerifiedEvent 대신 MascotAcquiredEvent를 구독하도록 이미 애플리케이션 코드는 변경됨 - V3 참고)

ALTER TABLE badges RENAME COLUMN required_visit_count TO required_mascot_count;
ALTER TABLE user_visit_counters RENAME TO user_mascot_counters;
ALTER TABLE user_mascot_counters RENAME COLUMN visit_count TO mascot_count;

-- 기존 3단계(BEGINNER=1/EXPLORER=5/MASTER=10)는 임시 placeholder였음(BadgeSeedConfig 주석 참고).
-- 기획 확정 10단계 기준값으로 교체. 아직 실사용자가 없는 개발 단계라 기존 UserBadge 참조 걱정 없이 재시딩한다.
DELETE FROM badges;

INSERT INTO badges (code, name, description, required_mascot_count, icon_url, created_at, updated_at) VALUES
    ('SEEDLING', '여행 새싹', '마스코트 여행을 시작했어요', 0, NULL, now(), now()),
    ('BEGINNER', '여행 입문자', '마스코트 5개를 모았어요', 5, NULL, now(), now()),
    ('NOVICE_EXPLORER', '초보 탐험가', '마스코트 10개를 모았어요', 10, NULL, now(), now()),
    ('REGION_COLLECTOR', '지역 수집가', '마스코트 15개를 모았어요', 15, NULL, now(), now()),
    ('PIONEER', '여행 개척자', '마스코트 20개를 모았어요', 20, NULL, now(), now()),
    ('EXPERT', '여행 전문가', '마스코트 25개를 모았어요', 25, NULL, now(), now()),
    ('VETERAN', '베테랑 여행자', '마스코트 30개를 모았어요', 30, NULL, now(), now()),
    ('NATIONWIDE_TRAVELER', '전국 여행가', '마스코트 45개를 모았어요', 45, NULL, now(), now()),
    ('MASTER_COLLECTOR', '마스터 컬렉터', '마스코트 50개를 모았어요', 50, NULL, now(), now()),
    ('LEGEND', '레전드 탐험가', '마스코트 55개 이상을 모았어요', 55, NULL, now(), now());

ALTER TABLE point_histories DROP CONSTRAINT point_histories_reason_check;
ALTER TABLE point_histories ADD CONSTRAINT point_histories_reason_check
    CHECK (((reason)::text = ANY ((ARRAY['MASCOT_COLLECT'::character varying, 'BADGE_COLLECT'::character varying, 'PRODUCT_COLLECT'::character varying, 'SHOP_PURCHASE'::character varying, 'ADMIN_ADJUST'::character varying, 'OTHER'::character varying])::text[])));
