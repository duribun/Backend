-- docs/point.md: PointReason.BADGE_COLLECT -> BADGE_ACQUIRED로 확정

UPDATE point_histories SET reason = 'BADGE_ACQUIRED' WHERE reason = 'BADGE_COLLECT';

ALTER TABLE point_histories DROP CONSTRAINT point_histories_reason_check;
ALTER TABLE point_histories ADD CONSTRAINT point_histories_reason_check
    CHECK (((reason)::text = ANY ((ARRAY['MASCOT_COLLECT'::character varying, 'BADGE_ACQUIRED'::character varying, 'PRODUCT_COLLECT'::character varying, 'SHOP_PURCHASE'::character varying, 'ADMIN_ADJUST'::character varying, 'OTHER'::character varying])::text[])));
