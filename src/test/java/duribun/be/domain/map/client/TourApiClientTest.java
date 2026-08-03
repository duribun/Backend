package duribun.be.domain.map.client;

import duribun.be.domain.map.config.TourApiProperties;
import duribun.be.domain.map.dto.TourApiAreaCodeResponse;
import duribun.be.domain.map.dto.TourApiDetailCommonResponse;
import duribun.be.domain.map.dto.TourApiDetailImageResponse;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.global.exception.TourApiCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TourApiClientTest {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2";
    private static final String SERVICE_KEY = "test-key%3D%3D";

    private MockRestServiceServer mockServer;
    private TourApiClient tourApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        TourApiProperties properties = new TourApiProperties(BASE_URL, SERVICE_KEY);
        tourApiClient = new TourApiClient(builder, JsonMapper.builder().build(), properties);
    }

    private String commonQuery(String path) {
        return BASE_URL + path
                + "?serviceKey=" + SERVICE_KEY
                + "&MobileOS=ETC&MobileApp=Duribun&_type=json&numOfRows=100&pageNo=1";
    }

    @Test
    void areaBasedList_결과가_배열이면_모두_파싱한다() {
        String url = commonQuery("/areaBasedList2") + "&arrange=A&contentTypeId=12&areaCode=32&sigunguCode=1";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": [
                                {"contentid":"1","contenttypeid":"12","title":"경포대","addr1":"강원 강릉시","mapx":"128.90","mapy":"37.79","firstimage":"https://a"},
                                {"contentid":"2","contenttypeid":"12","title":"오죽헌","addr1":"강원 강릉시","mapx":"128.87","mapy":"37.77","firstimage":"https://b"}
                              ] },
                              "numOfRows": 10, "pageNo": 1, "totalCount": 2
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TourApiItemResponse> items = tourApiClient.areaBasedList("32", "1", "12");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).title()).isEqualTo("경포대");
    }

    @Test
    void areaBasedList_sigunguCode가_없으면_쿼리에서_생략된다() {
        String url = commonQuery("/areaBasedList2") + "&arrange=A&contentTypeId=12&areaCode=1";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},
                        "body":{"items":"","numOfRows":10,"pageNo":1,"totalCount":0}}}
                        """, MediaType.APPLICATION_JSON));

        List<TourApiItemResponse> items = tourApiClient.areaBasedList("1", null, "12");

        assertThat(items).isEmpty();
    }

    @Test
    void 결과가_1건이면_item이_단일_객체로_와도_파싱한다() {
        String url = commonQuery("/searchKeyword2") + "&arrange=A&contentTypeId=12&keyword=%EA%B2%BD%ED%8F%AC%EB%8C%80";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": {"contentid":"1","contenttypeid":"12","title":"경포대","addr1":"강원 강릉시","mapx":"128.90","mapy":"37.79","firstimage":"https://a"} },
                              "numOfRows": 10, "pageNo": 1, "totalCount": 1
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TourApiItemResponse> items = tourApiClient.searchKeyword("경포대", "12");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).contentId()).isEqualTo("1");
    }

    @Test
    void locationBasedList_위경도와_반경을_쿼리에_담아_호출한다() {
        String url = commonQuery("/locationBasedList2") + "&arrange=E&contentTypeId=12&mapX=128.9&mapY=37.79&radius=5000";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},
                        "body":{"items":"","numOfRows":10,"pageNo":1,"totalCount":0}}}
                        """, MediaType.APPLICATION_JSON));

        List<TourApiItemResponse> items = tourApiClient.locationBasedList(37.79, 128.9, 5000, "12");

        assertThat(items).isEmpty();
    }

    @Test
    void detailCommon_정상_응답이면_상세정보를_반환한다() {
        String url = commonQuery("/detailCommon2") + "&contentId=1";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": {"contentid":"1","contenttypeid":"12","title":"경포대","addr1":"강원 강릉시","mapx":"128.90","mapy":"37.79","overview":"설명","firstimage":"https://a"} },
                              "numOfRows": 1, "pageNo": 1, "totalCount": 1
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<TourApiDetailCommonResponse> result = tourApiClient.detailCommon("1");

        assertThat(result).isPresent();
        assertThat(result.get().description()).isEqualTo("설명");
    }

    @Test
    void detailCommon_존재하지_않는_contentId면_빈값을_반환한다() {
        String url = commonQuery("/detailCommon2") + "&contentId=no-such-id";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"0000","resultMsg":"OK"},
                        "body":{"items":"","numOfRows":10,"pageNo":1,"totalCount":0}}}
                        """, MediaType.APPLICATION_JSON));

        Optional<TourApiDetailCommonResponse> result = tourApiClient.detailCommon("no-such-id");

        assertThat(result).isEmpty();
    }

    @Test
    void detailImage_이미지_목록을_반환한다() {
        String url = commonQuery("/detailImage2") + "&contentId=1&imageYN=Y";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": [
                                {"originimgurl":"https://a"},
                                {"originimgurl":"https://b"}
                              ] },
                              "numOfRows": 2, "pageNo": 1, "totalCount": 2
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TourApiDetailImageResponse> images = tourApiClient.detailImage("1");

        assertThat(images).extracting(TourApiDetailImageResponse::imageUrl)
                .containsExactly("https://a", "https://b");
    }

    @Test
    void areaCode_parentAreaCode가_없으면_시도목록을_조회한다() {
        String url = commonQuery("/areaCode2");
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": [
                                {"code":"1","name":"서울"},
                                {"code":"32","name":"강원특별자치도"}
                              ] },
                              "numOfRows": 2, "pageNo": 1, "totalCount": 2
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TourApiAreaCodeResponse> codes = tourApiClient.areaCode(null);

        assertThat(codes).extracting(TourApiAreaCodeResponse::name)
                .containsExactly("서울", "강원특별자치도");
    }

    @Test
    void areaCode_parentAreaCode가_있으면_쿼리에_포함해_호출한다() {
        String url = commonQuery("/areaCode2") + "&areaCode=32";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "header": { "resultCode": "0000", "resultMsg": "OK" },
                            "body": {
                              "items": { "item": {"code":"1","name":"강릉시"} },
                              "numOfRows": 1, "pageNo": 1, "totalCount": 1
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<TourApiAreaCodeResponse> codes = tourApiClient.areaCode("32");

        assertThat(codes).extracting(TourApiAreaCodeResponse::name).containsExactly("강릉시");
    }

    @Test
    void resultCode가_실패면_TourApiCallException을_던진다() {
        String url = commonQuery("/areaBasedList2") + "&arrange=A&contentTypeId=12&areaCode=1";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {"response":{"header":{"resultCode":"30","resultMsg":"SERVICE_KEY_IS_NOT_REGISTERED_ERROR"}}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> tourApiClient.areaBasedList("1", null, "12"))
                .isInstanceOf(TourApiCallException.class);
    }

    @Test
    void HTTP_에러_응답이면_TourApiCallException을_던진다() {
        String url = commonQuery("/areaBasedList2") + "&arrange=A&contentTypeId=12&areaCode=1";
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        assertThatThrownBy(() -> tourApiClient.areaBasedList("1", null, "12"))
                .isInstanceOf(TourApiCallException.class);
    }
}
