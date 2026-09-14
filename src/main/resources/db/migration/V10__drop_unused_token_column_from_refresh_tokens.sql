-- refresh_tokens.token은 RefreshToken 엔티티가 매핑하지 않는 컬럼이다.
-- 실제로 저장되는 값은 token_hash(해시)뿐이고, token(원문 저장용으로 보이는 컬럼)은
-- 어떤 코드에서도 채워주지 않아 NOT NULL 제약 때문에 모든 로그인이 insert 단계에서 실패했다.
-- 원문 토큰을 그대로 저장하는 건 보안상으로도 바람직하지 않으므로 컬럼 자체를 제거한다.
ALTER TABLE refresh_tokens
    DROP COLUMN token;
