-- docs/ISSUE-여행기록-스키마확장.md 후속: 사진 없이/장소 미등록으로도 기록을 남길 수 있도록
-- place_name/latitude/longitude를 nullable로 완화한다.
-- 세 값이 "다 있거나 다 없거나"만 유효해야 한다는 제약은 애플리케이션(RecordService) 레벨에서 검증한다.

ALTER TABLE travel_records ALTER COLUMN place_name DROP NOT NULL;
ALTER TABLE travel_records ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE travel_records ALTER COLUMN longitude DROP NOT NULL;
