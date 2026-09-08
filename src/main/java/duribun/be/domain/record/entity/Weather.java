package duribun.be.domain.record.entity;

/**
 * 여행 기록 작성 화면에서 사용자가 직접 선택하는 날씨 아이콘 (외부 날씨 API 연동 없음).
 * 근거: Figma "(7) 기록" 와이어프레임의 날씨 선택 드롭다운 6종.
 */
public enum Weather {
    CLEAR,
    SNOW,
    RAIN,
    THUNDER,
    WIND,
    CLOUDY
}
