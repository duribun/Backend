-- 칭호(배지) 기준값 2차 개편 — 10단계(V4) → 8단계로 축소
-- EXPERT(여행 전문가, 25), VETERAN(베테랑 여행자, 30) 두 단계를 제거하고
-- NATIONWIDE_TRAVELER/MASTER_TRAVELER(舊 MASTER_COLLECTOR)/LEGEND 기준값을 30/40/50으로 앞당김.
-- 아직 실사용자가 없는 개발 단계라 V4와 동일하게 기존 UserBadge 참조 걱정 없이 재시딩한다.

DELETE FROM badges;

INSERT INTO badges (code, name, description, required_mascot_count, icon_url, created_at, updated_at) VALUES
    ('SEEDLING', '여행 새싹', '마스코트 여행을 시작했어요', 0, NULL, now(), now()),
    ('BEGINNER', '여행 입문자', '마스코트 5개를 모았어요', 5, NULL, now(), now()),
    ('NOVICE_TRAVELER', '초보 여행가', '마스코트 10개를 모았어요', 10, NULL, now(), now()),
    ('REGION_COLLECTOR', '지역 수집가', '마스코트 15개를 모았어요', 15, NULL, now(), now()),
    ('PIONEER', '여행 개척자', '마스코트 20개를 모았어요', 20, NULL, now(), now()),
    ('NATIONWIDE_TRAVELER', '전국 여행가', '마스코트 30개를 모았어요', 30, NULL, now(), now()),
    ('MASTER_TRAVELER', '마스터 여행가', '마스코트 40개를 모았어요', 40, NULL, now(), now()),
    ('LEGEND', '레전드 여행가', '마스코트 50개 이상을 모았어요', 50, NULL, now(), now());
