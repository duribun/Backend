-- docs/mascot.md 8번 리네이밍 체크리스트: character -> mascot 용어 통일
-- (기획 문서 용어 "마스코트"와 온보딩 "기본 캐릭터(아바타)"의 명칭 충돌을 없애기 위함)

ALTER TABLE characters RENAME TO mascots;
ALTER TABLE user_characters RENAME TO user_mascots;
ALTER TABLE user_mascots RENAME COLUMN character_id TO mascot_id;

UPDATE point_histories SET reason = 'MASCOT_COLLECT' WHERE reason = 'CHARACTER_COLLECT';

ALTER TABLE point_histories DROP CONSTRAINT point_histories_reason_check;
ALTER TABLE point_histories ADD CONSTRAINT point_histories_reason_check
    CHECK (((reason)::text = ANY ((ARRAY['MASCOT_COLLECT'::character varying, 'PRODUCT_COLLECT'::character varying, 'SHOP_PURCHASE'::character varying, 'ADMIN_ADJUST'::character varying, 'OTHER'::character varying])::text[])));
