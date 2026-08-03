package duribun.be.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void InvalidSocialTokenException은_401을_반환한다() throws Exception {
        mockMvc.perform(get("/test/invalid-social-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("소셜 토큰이 유효하지 않습니다"));
    }

    @Test
    void UnsupportedProviderException은_400을_반환한다() throws Exception {
        mockMvc.perform(get("/test/unsupported-provider"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지원하지 않는 provider입니다"));
    }

    @Test
    void InvalidRefreshTokenException은_401을_반환한다() throws Exception {
        mockMvc.perform(get("/test/invalid-refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 유효하지 않습니다"));
    }

    @Test
    void RegionNotFoundException은_404를_반환한다() throws Exception {
        mockMvc.perform(get("/test/region-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 지역입니다"));
    }

    @Test
    void InsufficientPointException은_409를_반환한다() throws Exception {
        mockMvc.perform(get("/test/insufficient-point"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("포인트 잔액이 부족합니다"));
    }

    @Test
    void OptimisticLockingFailureException은_409를_반환한다() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("다른 요청에 의해 처리 중입니다. 잠시 후 다시 시도해주세요"));
    }

    @Test
    void AttractionNotFoundException은_404를_반환한다() throws Exception {
        mockMvc.perform(get("/test/attraction-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 관광지입니다"));
    }

    @Test
    void TourApiCallException은_502를_반환한다() throws Exception {
        mockMvc.perform(get("/test/tour-api-call-failed"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("TourAPI 호출에 실패했습니다"));
    }

    @Test
    void 요청_body_유효성_검증_실패는_400을_반환한다() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @RestController
    @RequestMapping("/test")
    @Validated
    static class ThrowingTestController {

        @GetMapping("/invalid-social-token")
        public void invalidSocialToken() {
            throw new InvalidSocialTokenException("소셜 토큰이 유효하지 않습니다");
        }

        @GetMapping("/unsupported-provider")
        public void unsupportedProvider() {
            throw new UnsupportedProviderException("지원하지 않는 provider입니다");
        }

        @GetMapping("/invalid-refresh-token")
        public void invalidRefreshToken() {
            throw new InvalidRefreshTokenException("리프레시 토큰이 유효하지 않습니다");
        }

        @GetMapping("/region-not-found")
        public void regionNotFound() {
            throw new RegionNotFoundException("존재하지 않는 지역입니다");
        }

        @GetMapping("/insufficient-point")
        public void insufficientPoint() {
            throw new InsufficientPointException("포인트 잔액이 부족합니다");
        }

        @GetMapping("/optimistic-lock")
        public void optimisticLock() {
            throw new OptimisticLockingFailureException("stale version");
        }

        @GetMapping("/attraction-not-found")
        public void attractionNotFound() {
            throw new AttractionNotFoundException("존재하지 않는 관광지입니다");
        }

        @GetMapping("/tour-api-call-failed")
        public void tourApiCallFailed() {
            throw new TourApiCallException("TourAPI 호출에 실패했습니다");
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {
        }
    }

    record TestRequest(@NotBlank String token) {
    }
}
