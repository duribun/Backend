-- docs/profile.md: 온보딩 프로필(닉네임/생년월일/성별) 저장을 위해 users에 컬럼 추가

ALTER TABLE users ADD COLUMN birth_date date;
ALTER TABLE users ADD COLUMN gender character varying(255);

ALTER TABLE users ADD CONSTRAINT users_gender_check
    CHECK (((gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying])::text[])));
