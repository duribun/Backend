package duribun.be.domain.setting.dto;

public record NicknameCheckResponse(boolean available) {

    public static NicknameCheckResponse of(boolean available) {
        return new NicknameCheckResponse(available);
    }
}
