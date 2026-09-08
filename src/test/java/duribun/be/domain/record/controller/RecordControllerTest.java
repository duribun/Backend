package duribun.be.domain.record.controller;

import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.entity.Weather;
import duribun.be.domain.record.repository.RecordRepository;
import duribun.be.domain.user.entity.Role;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecordControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private RecordRepository recordRepository;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    private TravelRecord saveRecord(Long userId, String title, LocalDate visitedAt) {
        return recordRepository.saveAndFlush(
                TravelRecord.create(userId, title, "내용", visitedAt, "장소",
                        35.0968, 129.0306, Weather.CLEAR, 24, Mood.HAPPY)
        );
    }

    private Map<String, Object> validCreateBody() {
        return Map.of(
                "title", "부산 여행",
                "content", "자갈치시장 방문",
                "visitedAt", "2026-08-09",
                "placeName", "부산 자갈치시장",
                "latitude", 35.0968,
                "longitude", 129.0306,
                "weather", "CLEAR",
                "temperature", 24,
                "mood", "HAPPY"
        );
    }

    // ── 인증 검사 ─────────────────────────────────────────────────────────────

    @Nested
    class 인증_검사 {

        @ParameterizedTest(name = "{0} {1} 요청시 401")
        @CsvSource({
            "POST,/api/records",
            "GET,/api/records/me",
            "GET,/api/records/1",
            "PATCH,/api/records/1",
            "PATCH,/api/records/1/favorite",
            "DELETE,/api/records/1",
            "POST,/api/records/images/presigned-url?extension=jpg"
        })
        void 인증없이_접근하면_401을_반환한다(String httpMethod, String url) throws Exception {
            mockMvc.perform(buildRequest(httpMethod, url))
                    .andExpect(status().isUnauthorized());
        }

        private MockHttpServletRequestBuilder buildRequest(String method, String url) {
            return switch (method) {
                case "GET" -> get(url);
                case "POST" -> post(url).contentType(MediaType.APPLICATION_JSON).content("{}");
                case "PATCH" -> patch(url).contentType(MediaType.APPLICATION_JSON).content("{}");
                case "DELETE" -> delete(url);
                default -> throw new IllegalArgumentException(method);
            };
        }
    }

    // ── 기록 생성 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_생성 {

        static Stream<Arguments> missingFieldRequests() {
            return Stream.of(
                    Arguments.of("title", "title"),
                    Arguments.of("content", "content"),
                    Arguments.of("visitedAt", "visitedAt"),
                    Arguments.of("placeName", "placeName"),
                    Arguments.of("latitude", "latitude"),
                    Arguments.of("longitude", "longitude"),
                    Arguments.of("weather", "weather"),
                    Arguments.of("temperature", "temperature"),
                    Arguments.of("mood", "mood")
            );
        }

        @Test
        void 생성_성공시_201과_생성된_기록을_반환한다() throws Exception {
            String body = objectMapper.writeValueAsString(validCreateBody());

            mockMvc.perform(post("/api/records")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("부산 여행"))
                    .andExpect(jsonPath("$.placeName").value("부산 자갈치시장"))
                    .andExpect(jsonPath("$.weather").value("CLEAR"))
                    .andExpect(jsonPath("$.mood").value("HAPPY"))
                    .andExpect(jsonPath("$.favorite").value(false))
                    .andExpect(jsonPath("$.imageUrls").isEmpty());
        }

        @Test
        void 사진_URL_4장까지_포함해_생성할_수_있다() throws Exception {
            Map<String, Object> body = new java.util.HashMap<>(validCreateBody());
            body.put("imageUrls", java.util.List.of("https://a.jpg", "https://b.jpg", "https://c.jpg", "https://d.jpg"));

            mockMvc.perform(post("/api/records")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.imageUrls.length()").value(4));
        }

        @Test
        void 사진이_4장을_초과하면_400을_반환한다() throws Exception {
            Map<String, Object> body = new java.util.HashMap<>(validCreateBody());
            body.put("imageUrls", java.util.List.of("https://a.jpg", "https://b.jpg", "https://c.jpg", "https://d.jpg", "https://e.jpg"));

            mockMvc.perform(post("/api/records")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest(name = "{0} 누락시 400")
        @MethodSource("missingFieldRequests")
        void 필수_필드_누락시_400을_반환한다(String missingField, String fieldToRemove) throws Exception {
            Map<String, Object> body = new java.util.HashMap<>(validCreateBody());
            body.remove(fieldToRemove);

            mockMvc.perform(post("/api/records")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── 기록 목록 조회 ──────────────────────────────────────────────────────────

    @Nested
    class 기록_목록_조회 {

        @Test
        void 전체_목록을_조회하면_본인_기록만_반환한다() throws Exception {
            saveRecord(1L, "첫 번째", LocalDate.of(2026, 7, 1));
            saveRecord(1L, "두 번째", LocalDate.of(2026, 8, 1));
            saveRecord(2L, "다른 유저", LocalDate.of(2026, 8, 1));

            mockMvc.perform(get("/api/records/me")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void 년월_필터로_해당_월의_기록만_조회한다() throws Exception {
            saveRecord(1L, "7월 기록", LocalDate.of(2026, 7, 15));
            saveRecord(1L, "8월 기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(get("/api/records/me?year=2026&month=8")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("8월 기록"));
        }

        @Test
        void 목록은_visitedAt_오름차순으로_정렬된다() throws Exception {
            saveRecord(1L, "나중 기록", LocalDate.of(2026, 8, 15));
            saveRecord(1L, "이전 기록", LocalDate.of(2026, 8, 1));

            mockMvc.perform(get("/api/records/me")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("이전 기록"))
                    .andExpect(jsonPath("$[1].title").value("나중 기록"));
        }

        @Test
        void 즐겨찾기한_기록이_최상단에_온다() throws Exception {
            saveRecord(1L, "이전 기록", LocalDate.of(2026, 8, 1));
            TravelRecord later = saveRecord(1L, "나중 기록", LocalDate.of(2026, 8, 15));

            mockMvc.perform(patch("/api/records/" + later.getId() + "/favorite")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.favorite").value(true));

            mockMvc.perform(get("/api/records/me")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("나중 기록"))
                    .andExpect(jsonPath("$[0].favorite").value(true));
        }
    }

    // ── 기록 상세 조회 ──────────────────────────────────────────────────────────

    @Nested
    class 기록_상세_조회 {

        @Test
        void 본인_기록을_조회한다() throws Exception {
            TravelRecord record = saveRecord(1L, "부산 여행", LocalDate.of(2026, 8, 9));

            mockMvc.perform(get("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(record.getId()))
                    .andExpect(jsonPath("$.title").value("부산 여행"))
                    .andExpect(jsonPath("$.content").value("내용"))
                    .andExpect(jsonPath("$.latitude").value(35.0968))
                    .andExpect(jsonPath("$.longitude").value(129.0306));
        }

        @Test
        void 타인의_기록_조회시_403을_반환한다() throws Exception {
            TravelRecord record = saveRecord(2L, "타인 기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(get("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void 존재하지_않는_기록_조회시_404를_반환한다() throws Exception {
            mockMvc.perform(get("/api/records/99999")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isNotFound());
        }
    }

    // ── 기록 수정 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_수정 {

        @Test
        void 수정_성공시_변경된_내용을_반환한다() throws Exception {
            TravelRecord record = saveRecord(1L, "원래 제목", LocalDate.of(2026, 8, 9));
            String body = objectMapper.writeValueAsString(Map.of("title", "수정된 제목"));

            mockMvc.perform(patch("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.content").value("내용"));
        }

        @Test
        void 타인의_기록_수정시_403을_반환한다() throws Exception {
            TravelRecord record = saveRecord(2L, "타인 기록", LocalDate.of(2026, 8, 9));
            String body = objectMapper.writeValueAsString(Map.of("title", "수정 시도"));

            mockMvc.perform(patch("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }
    }

    // ── 즐겨찾기 ───────────────────────────────────────────────────────────────

    @Nested
    class 즐겨찾기 {

        @Test
        void 토글하면_즐겨찾기_상태가_반전된다() throws Exception {
            TravelRecord record = saveRecord(1L, "기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(patch("/api/records/" + record.getId() + "/favorite")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.favorite").value(true));

            mockMvc.perform(patch("/api/records/" + record.getId() + "/favorite")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.favorite").value(false));
        }

        @Test
        void 타인의_기록이면_403을_반환한다() throws Exception {
            TravelRecord record = saveRecord(2L, "타인 기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(patch("/api/records/" + record.getId() + "/favorite")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isForbidden());
        }
    }

    // ── 기록 삭제 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_삭제 {

        @Test
        void 삭제_성공시_204를_반환한다() throws Exception {
            TravelRecord record = saveRecord(1L, "삭제할 기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(delete("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void 타인의_기록_삭제시_403을_반환한다() throws Exception {
            TravelRecord record = saveRecord(2L, "타인 기록", LocalDate.of(2026, 8, 9));

            mockMvc.perform(delete("/api/records/" + record.getId())
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isForbidden());
        }
    }
}
