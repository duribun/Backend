package duribun.be.domain.shop.controller;

import duribun.be.domain.point.service.PointReason;
import duribun.be.domain.point.service.PointServiceImpl;
import duribun.be.domain.shop.entity.Item;
import duribun.be.domain.shop.entity.ItemCategory;
import duribun.be.domain.shop.entity.UserItem;
import duribun.be.domain.shop.repository.ItemRepository;
import duribun.be.domain.shop.repository.UserItemRepository;
import duribun.be.domain.user.entity.Role;
import duribun.be.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShopControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserItemRepository userItemRepository;
    @Autowired private PointServiceImpl pointService;

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Role.USER);
    }

    private Item saveItem(String name, int price, ItemCategory category) {
        return itemRepository.saveAndFlush(Item.create(name, "설명", price, null, category));
    }

    private UserItem saveUserItem(Long userId, Item item) {
        return userItemRepository.saveAndFlush(UserItem.create(userId, item.getId(), item.getCategory(), LocalDateTime.now()));
    }

    // ── 인증 검사 ─────────────────────────────────────────────────────────────

    @Nested
    class 인증_검사 {

        @ParameterizedTest(name = "{0} {1} 요청시 401")
        @CsvSource({
            "GET,/api/shop/items",
            "GET,/api/shop/items/me",
            "POST,/api/shop/items/1/purchase",
            "PATCH,/api/shop/items/1/equip"
        })
        void 인증없이_접근하면_401을_반환한다(String httpMethod, String url) throws Exception {
            mockMvc.perform(buildRequest(httpMethod, url))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 인증없이_접근하면_명세와_동일한_형식의_에러_바디를_반환한다() throws Exception {
            mockMvc.perform(get("/api/shop/items"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다"));
        }

        private MockHttpServletRequestBuilder buildRequest(String method, String url) {
            return switch (method) {
                case "GET" -> get(url);
                case "POST" -> post(url);
                case "PATCH" -> patch(url);
                default -> throw new IllegalArgumentException(method);
            };
        }
    }

    // ── 전체 아이템 조회 ──────────────────────────────────────────────────────

    @Nested
    class 전체_아이템_조회 {

        @Test
        void 전체_아이템_목록을_조회한다() throws Exception {
            saveItem("여행자 선글라스", 500, ItemCategory.GLASSES);
            saveItem("등산 배낭", 600, ItemCategory.BAG);

            mockMvc.perform(get("/api/shop/items")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // ── 내 아이템 조회 ────────────────────────────────────────────────────────

    @Nested
    class 내_아이템_조회 {

        @Test
        void 다른_유저의_아이템은_섞이지_않고_본인것만_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);
            saveUserItem(1L, item);
            saveUserItem(2L, item);

            mockMvc.perform(get("/api/shop/items/me")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    // ── 아이템 구매 ───────────────────────────────────────────────────────────

    @Nested
    class 아이템_구매 {

        @Test
        void 구매_성공시_200과_잔여포인트를_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);
            pointService.earn(1L, 1000, PointReason.MASCOT_COLLECT);

            mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemId").value(item.getId()))
                    .andExpect(jsonPath("$.remainingPoints").value(500));
        }

        @Test
        void 존재하지_않는_아이템이면_404를_반환한다() throws Exception {
            mockMvc.perform(post("/api/shop/items/99999/purchase")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 아이템입니다."));
        }

        @Test
        void 이미_구매한_아이템_재구매시_409를_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);
            pointService.earn(1L, 2000, PointReason.MASCOT_COLLECT);
            saveUserItem(1L, item);

            mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("이미 구매한 아이템입니다."));
        }

        @Test
        void 포인트가_부족하면_409를_반환한다() throws Exception {
            Item item = saveItem("비싼 캐리어", 9999, ItemCategory.CARRIER);

            mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("포인트 잔액이 부족합니다"));
        }
    }

    // ── 아이템 착용 ───────────────────────────────────────────────────────────

    @Nested
    class 아이템_착용 {

        @Test
        void 착용_성공시_isEquipped가_true를_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);
            saveUserItem(1L, item);

            mockMvc.perform(patch("/api/shop/items/" + item.getId() + "/equip")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isEquipped").value(true));
        }

        @Test
        void 착용_상태에서_재호출하면_isEquipped가_false를_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);
            UserItem userItem = UserItem.create(1L, item.getId(), item.getCategory(), LocalDateTime.now());
            userItem.equip();
            userItemRepository.saveAndFlush(userItem);

            mockMvc.perform(patch("/api/shop/items/" + item.getId() + "/equip")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isEquipped").value(false));
        }

        @Test
        void 미구매_아이템_착용시_403을_반환한다() throws Exception {
            Item item = saveItem("선글라스", 500, ItemCategory.GLASSES);

            mockMvc.perform(patch("/api/shop/items/" + item.getId() + "/equip")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("구매하지 않은 아이템입니다."));
        }

        @Test
        void 같은_카테고리_다른_아이템_착용시_기존_착용_아이템이_해제된다() throws Exception {
            Item item1 = saveItem("선글라스1", 500, ItemCategory.GLASSES);
            Item item2 = saveItem("선글라스2", 800, ItemCategory.GLASSES);

            UserItem ui1 = UserItem.create(1L, item1.getId(), item1.getCategory(), LocalDateTime.now());
            ui1.equip();
            userItemRepository.saveAndFlush(ui1);
            saveUserItem(1L, item2);

            mockMvc.perform(patch("/api/shop/items/" + item2.getId() + "/equip")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isEquipped").value(true));

            mockMvc.perform(get("/api/shop/items/me")
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(1L)))
                    .andExpect(jsonPath("$[?(@.id == " + item1.getId() + ")].isEquipped").value(false))
                    .andExpect(jsonPath("$[?(@.id == " + item2.getId() + ")].isEquipped").value(true));
        }
    }
}
