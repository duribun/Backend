package duribun.be.domain.record.service;

import duribun.be.common.TimeProvider;
import duribun.be.domain.record.client.S3ImageClient;
import duribun.be.domain.record.dto.CreateRecordRequest;
import duribun.be.domain.record.dto.PresignedImageUploadResponse;
import duribun.be.domain.record.dto.RecordResponse;
import duribun.be.domain.record.dto.RecordSummaryResponse;
import duribun.be.domain.record.dto.UpdateRecordRequest;
import duribun.be.domain.record.entity.RecordImage;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.repository.RecordImageRepository;
import duribun.be.domain.record.repository.RecordRepository;
import duribun.be.global.exception.InvalidImageExtensionException;
import duribun.be.global.exception.InvalidPlaceException;
import duribun.be.global.exception.RecordForbiddenException;
import duribun.be.global.exception.RecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecordService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final RecordRepository recordRepository;
    private final RecordImageRepository recordImageRepository;
    private final S3ImageClient s3ImageClient;
    private final TimeProvider timeProvider;

    public RecordService(RecordRepository recordRepository,
                          RecordImageRepository recordImageRepository,
                          S3ImageClient s3ImageClient,
                          TimeProvider timeProvider) {
        this.recordRepository = recordRepository;
        this.recordImageRepository = recordImageRepository;
        this.s3ImageClient = s3ImageClient;
        this.timeProvider = timeProvider;
    }

    public PresignedImageUploadResponse issuePresignedUrl(Long userId, String extension) {
        String normalized = extension == null ? "" : extension.toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(normalized)) {
            throw new InvalidImageExtensionException("지원하지 않는 이미지 형식입니다: " + extension);
        }
        S3ImageClient.PresignedImageUpload presigned = s3ImageClient.issuePresignedUrl(userId, normalized);
        return new PresignedImageUploadResponse(presigned.uploadUrl(), presigned.imageUrl());
    }

    public RecordResponse createRecord(Long userId, CreateRecordRequest request) {
        validatePlaceConsistency(request.placeName(), request.latitude(), request.longitude());
        TravelRecord record = TravelRecord.create(
                userId,
                request.title(),
                request.content(),
                request.visitedAt(),
                request.placeName(),
                request.latitude(),
                request.longitude(),
                request.weather(),
                request.temperature(),
                request.mood()
        );
        TravelRecord saved = recordRepository.save(record);
        List<String> imageUrls = saveImages(saved.getId(), request.imageUrls());
        return RecordResponse.of(saved, imageUrls);
    }

    @Transactional(readOnly = true)
    public List<RecordSummaryResponse> getMyRecords(Long userId, Integer year, Integer month) {
        List<TravelRecord> records = findRecords(userId, year, month);
        Map<Long, String> thumbnails = findThumbnails(records);
        return sortByFavoriteThenVisitedAt(records).stream()
                .map(record -> RecordSummaryResponse.of(record, thumbnails.get(record.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RecordResponse getRecord(Long userId, Long recordId) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        return RecordResponse.of(record, findImageUrls(recordId));
    }

    public RecordResponse updateRecord(Long userId, Long recordId, UpdateRecordRequest request) {
        validatePlaceConsistency(request.placeName(), request.latitude(), request.longitude());
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        record.update(request.title(), request.content(), request.visitedAt(), request.placeName(),
                request.latitude(), request.longitude(), request.weather(), request.temperature(), request.mood());

        List<String> imageUrls;
        if (request.imageUrls() != null) {
            recordImageRepository.deleteByRecordId(recordId);
            imageUrls = saveImages(recordId, request.imageUrls());
        } else {
            imageUrls = findImageUrls(recordId);
        }
        return RecordResponse.of(record, imageUrls);
    }

    public RecordResponse toggleFavorite(Long userId, Long recordId) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        record.toggleFavorite(timeProvider.now());
        return RecordResponse.of(record, findImageUrls(recordId));
    }

    public void deleteRecord(Long userId, Long recordId) {
        TravelRecord record = findRecord(recordId);
        validateOwner(record, userId);
        List<RecordImage> images = recordImageRepository.findByRecordIdOrderBySortOrderAsc(recordId);
        images.forEach(image -> s3ImageClient.deleteImage(image.getImageUrl()));
        recordImageRepository.deleteByRecordId(recordId);
        recordRepository.delete(record);
    }

    private List<TravelRecord> findRecords(Long userId, Integer year, Integer month) {
        if (year == null) {
            return recordRepository.findByUserIdOrderByVisitedAtAsc(userId);
        }
        LocalDate from;
        LocalDate to;
        if (month == null) {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        } else {
            YearMonth yearMonth = YearMonth.of(year, month);
            from = yearMonth.atDay(1);
            to = yearMonth.atEndOfMonth();
        }
        return recordRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(userId, from, to);
    }

    /**
     * 즐겨찾기한 기록을 먼저(즐겨찾기 누른 시각 오름차순 = 과거→미래), 나머지는 기존 순서(visitedAt 오름차순)대로.
     * 근거: Figma 와이어프레임 주석 "별을 여러개 선택했다면 별을 누른 순서대로(오름차순: 과거→미래)".
     */
    private List<TravelRecord> sortByFavoriteThenVisitedAt(List<TravelRecord> records) {
        List<TravelRecord> favorited = records.stream()
                .filter(TravelRecord::isFavorite)
                .sorted(Comparator.comparing(TravelRecord::getFavoritedAt))
                .toList();
        List<TravelRecord> others = records.stream()
                .filter(record -> !record.isFavorite())
                .toList();

        List<TravelRecord> result = new ArrayList<>(favorited.size() + others.size());
        result.addAll(favorited);
        result.addAll(others);
        return result;
    }

    private Map<Long, String> findThumbnails(List<TravelRecord> records) {
        List<Long> recordIds = records.stream().map(TravelRecord::getId).toList();
        if (recordIds.isEmpty()) {
            return Map.of();
        }
        return recordImageRepository.findByRecordIdInOrderByRecordIdAscSortOrderAsc(recordIds).stream()
                .collect(Collectors.toMap(RecordImage::getRecordId, RecordImage::getImageUrl, (first, second) -> first));
    }

    private List<String> findImageUrls(Long recordId) {
        return recordImageRepository.findByRecordIdOrderBySortOrderAsc(recordId).stream()
                .map(RecordImage::getImageUrl)
                .toList();
    }

    private List<String> saveImages(Long recordId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<RecordImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(RecordImage.create(recordId, imageUrls.get(i), i));
        }
        recordImageRepository.saveAll(images);
        return imageUrls;
    }

    /**
     * placeName/latitude/longitude는 장소를 등록하지 않은 기록(사진만/글만 있는 기록)을 허용하기 위해
     * 개별 필드로는 optional이지만, "셋 다 없거나(장소 미등록) 셋 다 있거나(장소 등록)"만 유효하다.
     * 위도/경도는 함께 있어야만 장소가 특정되는 값이라 하나만 오는 상태는 의미가 없기 때문이다.
     */
    private void validatePlaceConsistency(String placeName, Double latitude, Double longitude) {
        boolean hasPlaceName = placeName != null && !placeName.isBlank();
        boolean hasLatitude = latitude != null;
        boolean hasLongitude = longitude != null;
        int presentCount = (hasPlaceName ? 1 : 0) + (hasLatitude ? 1 : 0) + (hasLongitude ? 1 : 0);
        if (presentCount != 0 && presentCount != 3) {
            throw new InvalidPlaceException("장소명/위도/경도는 모두 입력하거나 모두 비워야 합니다.");
        }
    }

    private TravelRecord findRecord(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException("존재하지 않는 기록입니다."));
    }

    private void validateOwner(TravelRecord record, Long userId) {
        if (!record.getUserId().equals(userId)) {
            throw new RecordForbiddenException("본인의 기록만 접근할 수 있습니다.");
        }
    }
}
