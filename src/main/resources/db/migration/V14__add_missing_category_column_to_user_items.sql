-- user_items.category 컬럼이 로컬 개발 DB에서는 Hibernate ddl-auto: update(application-dev.yml)가
-- 자동으로 만들어줬지만, Flyway 마이그레이션에는 애초에 누락되어 있었다
-- (V1__baseline.sql 작성 당시 items에는 category가 있었는데 user_items에는 빠짐).
-- 운영(application-prod.yml)은 ddl-auto: validate라 Hibernate가 스키마를 만들어주지 않고
-- 검증만 하기 때문에, 배포 시 "missing column [category] in table [user_items]"로 기동 실패했다.
-- 최초 운영 배포 시점이라 user_items에 데이터가 없어 NOT NULL로 바로 추가 가능하다.

ALTER TABLE user_items ADD COLUMN category character varying(20) NOT NULL;

ALTER TABLE user_items ADD CONSTRAINT user_items_category_check
    CHECK (((category)::text = ANY ((ARRAY['TOP'::character varying, 'BOTTOM'::character varying, 'HAT'::character varying, 'SHOES'::character varying, 'ACCESSORY'::character varying])::text[])));
