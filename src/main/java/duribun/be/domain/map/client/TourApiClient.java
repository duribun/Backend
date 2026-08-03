package duribun.be.domain.map.client;

import duribun.be.domain.map.config.TourApiProperties;
import duribun.be.domain.map.dto.TourApiAreaCodeResponse;
import duribun.be.domain.map.dto.TourApiDetailCommonResponse;
import duribun.be.domain.map.dto.TourApiDetailImageResponse;
import duribun.be.domain.map.dto.TourApiItemResponse;
import duribun.be.global.exception.TourApiCallException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class TourApiClient {

    private static final String MOBILE_OS = "ETC";
    private static final String MOBILE_APP = "Duribun";
    private static final int DEFAULT_NUM_OF_ROWS = 100;
    private static final String SUCCESS_RESULT_CODE = "0000";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TourApiProperties properties;

    public TourApiClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
                          TourApiProperties properties) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public List<TourApiItemResponse> areaBasedList(String areaCode, String sigunguCode, String contentTypeId) {
        StringBuilder query = new StringBuilder(commonQuery("/areaBasedList2"))
                .append("&arrange=A")
                .append("&contentTypeId=").append(encode(contentTypeId))
                .append("&areaCode=").append(encode(areaCode));
        if (sigunguCode != null && !sigunguCode.isBlank()) {
            query.append("&sigunguCode=").append(encode(sigunguCode));
        }
        return callForList(query.toString(), TourApiItemResponse.class);
    }

    public List<TourApiItemResponse> locationBasedList(double latitude, double longitude, int radiusMeters,
                                                         String contentTypeId) {
        String query = commonQuery("/locationBasedList2")
                + "&arrange=E"
                + "&contentTypeId=" + encode(contentTypeId)
                + "&mapX=" + longitude
                + "&mapY=" + latitude
                + "&radius=" + radiusMeters;
        return callForList(query, TourApiItemResponse.class);
    }

    public List<TourApiItemResponse> searchKeyword(String keyword, String contentTypeId) {
        String query = commonQuery("/searchKeyword2")
                + "&arrange=A"
                + "&contentTypeId=" + encode(contentTypeId)
                + "&keyword=" + encode(keyword);
        return callForList(query, TourApiItemResponse.class);
    }

    public Optional<TourApiDetailCommonResponse> detailCommon(String contentId) {
        String query = commonQuery("/detailCommon2") + "&contentId=" + encode(contentId);
        return callForList(query, TourApiDetailCommonResponse.class).stream().findFirst();
    }

    public List<TourApiDetailImageResponse> detailImage(String contentId) {
        String query = commonQuery("/detailImage2")
                + "&contentId=" + encode(contentId)
                + "&imageYN=Y";
        return callForList(query, TourApiDetailImageResponse.class);
    }

    public List<TourApiAreaCodeResponse> areaCode(String parentAreaCode) {
        String query = commonQuery("/areaCode2");
        if (parentAreaCode != null && !parentAreaCode.isBlank()) {
            query += "&areaCode=" + encode(parentAreaCode);
        }
        return callForList(query, TourApiAreaCodeResponse.class);
    }

    private String commonQuery(String path) {
        return path
                + "?serviceKey=" + properties.serviceKey()
                + "&MobileOS=" + MOBILE_OS
                + "&MobileApp=" + MOBILE_APP
                + "&_type=json"
                + "&numOfRows=" + DEFAULT_NUM_OF_ROWS
                + "&pageNo=1";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> List<T> callForList(String pathAndQuery, Class<T> itemType) {
        JsonNode root = request(pathAndQuery);
        return extractItems(root, itemType);
    }

    private JsonNode request(String pathAndQuery) {
        JsonNode root;
        try {
            root = restClient.get()
                    .uri(URI.create(properties.baseUrl() + pathAndQuery))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new TourApiCallException("TourAPI 호출에 실패했습니다");
        }
        if (root == null) {
            throw new TourApiCallException("TourAPI 응답이 비어있습니다");
        }
        String resultCode = root.path("response").path("header").path("resultCode").asText("");
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw new TourApiCallException("TourAPI 응답 오류: "
                    + root.path("response").path("header").path("resultMsg").asText("알 수 없는 오류"));
        }
        return root;
    }

    private <T> List<T> extractItems(JsonNode root, Class<T> itemType) {
        JsonNode itemNode = root.path("response").path("body").path("items").path("item");
        if (itemNode.isMissingNode() || itemNode.isNull()
                || (itemNode.isTextual() && itemNode.asText().isBlank())) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        if (itemNode.isArray()) {
            itemNode.forEach(node -> result.add(objectMapper.convertValue(node, itemType)));
        } else if (itemNode.isObject()) {
            result.add(objectMapper.convertValue(itemNode, itemType));
        }
        return result;
    }
}
