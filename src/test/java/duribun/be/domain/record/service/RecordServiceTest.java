package duribun.be.domain.record.service;

import duribun.be.common.TestEntityUtils;
import duribun.be.common.TimeProvider;
import duribun.be.domain.record.client.S3ImageClient;
import duribun.be.domain.record.dto.CreateRecordRequest;
import duribun.be.domain.record.dto.PresignedImageUploadResponse;
import duribun.be.domain.record.dto.RecordResponse;
import duribun.be.domain.record.dto.RecordSummaryResponse;
import duribun.be.domain.record.dto.UpdateRecordRequest;
import duribun.be.domain.record.entity.Mood;
import duribun.be.domain.record.entity.RecordImage;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.entity.Weather;
import duribun.be.domain.record.repository.RecordImageRepository;
import duribun.be.domain.record.repository.RecordRepository;
import duribun.be.global.exception.InvalidImageExtensionException;
import duribun.be.global.exception.InvalidPlaceException;
import duribun.be.global.exception.RecordForbiddenException;
import duribun.be.global.exception.RecordNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private RecordImageRepository recordImageRepository;
    @Mock private S3ImageClient s3ImageClient;
    @Mock private TimeProvider timeProvider;

    private RecordService recordService;

    @BeforeEach
    void setUp() {
        recordService = new RecordService(recordRepository, recordImageRepository, s3ImageClient, timeProvider);
    }

    private TravelRecord recordWithId(Long id, Long userId) {
        TravelRecord record = TravelRecord.create(
                userId, "제목", "내용",
                LocalDate.of(2026, 8, 9), "부산 자갈치시장",
                35.0968, 129.0306, Weather.CLEAR, 24, Mood.HAPPY
        );
        TestEntityUtils.setId(record, id);
        return record;
    }

    private CreateRecordRequest createRequest(List<String> imageUrls) {
        return new CreateRecordRequest(
                "부산 여행", "자갈치시장 방문", imageUrls,
                LocalDate.of(2026, 8, 9), "부산 자갈치시장",
                35.0968, 129.0306, Weather.CLEAR, 24, Mood.HAPPY
        );
    }

    private CreateRecordRequest createRequestWithPlace(String placeName, Double latitude, Double longitude) {
        return new CreateRecordRequest(
                "부산 여행", "자갈치시장 방문", null,
                LocalDate.of(2026, 8, 9), placeName,
                latitude, longitude, Weather.CLEAR, 24, Mood.HAPPY
        );
    }

    // ── 사진 업로드 presigned URL ────────────────────────────────────────────

    @Nested
    class 사진_업로드_presigned_URL {

        @Test
        void 허용된_확장자면_presigned_URL을_발급한다() {
            when(s3ImageClient.issuePresignedUrl(1L, "jpg"))
                    .thenReturn(new S3ImageClient.PresignedImageUpload("https://upload", "https://image"));

            PresignedImageUploadResponse response = recordService.issuePresignedUrl(1L, "JPG");

            assertThat(response.uploadUrl()).isEqualTo("https://upload");
            assertThat(response.imageUrl()).isEqualTo("https://image");
        }

        @Test
        void 허용되지_않은_확장자면_예외를_던진다() {
            assertThatThrownBy(() -> recordService.issuePresignedUrl(1L, "gif"))
                    .isInstanceOf(InvalidImageExtensionException.class);
        }
    }

    // ── 기록 생성 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_생성 {

        @Test
        void 기록을_저장하고_RecordResponse를_반환한다() {
            TravelRecord saved = recordWithId(1L, 1L);
            when(recordRepository.save(any(TravelRecord.class))).thenReturn(saved);

            RecordResponse response = recordService.createRecord(1L, createRequest(null));

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.imageUrls()).isEmpty();
        }

        @Test
        void 저장할_기록에_요청자의_userId가_설정된다() {
            when(recordRepository.save(any(TravelRecord.class))).thenAnswer(inv -> {
                TravelRecord record = inv.getArgument(0);
                TestEntityUtils.setId(record, 1L);
                return record;
            });

            recordService.createRecord(42L, createRequest(null));

            ArgumentCaptor<TravelRecord> captor = ArgumentCaptor.forClass(TravelRecord.class);
            verify(recordRepository).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(42L);
            assertThat(captor.getValue().getTitle()).isEqualTo("부산 여행");
        }

        @Test
        void 사진_URL_목록이_있으면_순서대로_RecordImage를_저장한다() {
            TravelRecord saved = recordWithId(1L, 1L);
            when(recordRepository.save(any(TravelRecord.class))).thenReturn(saved);
            List<String> imageUrls = List.of("https://a.jpg", "https://b.jpg");

            RecordResponse response = recordService.createRecord(1L, createRequest(imageUrls));

            ArgumentCaptor<List<RecordImage>> captor = ArgumentCaptor.forClass(List.class);
            verify(recordImageRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
            assertThat(captor.getValue().get(0).getSortOrder()).isEqualTo(0);
            assertThat(captor.getValue().get(1).getSortOrder()).isEqualTo(1);
            assertThat(response.imageUrls()).containsExactly("https://a.jpg", "https://b.jpg");
        }

        @Test
        void 사진_URL_목록이_빈_배열이면_사진_없이_저장된다() {
            TravelRecord saved = recordWithId(1L, 1L);
            when(recordRepository.save(any(TravelRecord.class))).thenReturn(saved);

            RecordResponse response = recordService.createRecord(1L, createRequest(List.of()));

            verify(recordImageRepository, never()).saveAll(any());
            assertThat(response.imageUrls()).isEmpty();
        }
    }

    // ── 장소(placeName/latitude/longitude) 검증 ───────────────────────────────

    @Nested
    class 장소_검증 {

        @Test
        void 장소_필드가_모두_없으면_장소_없는_기록으로_저장된다() {
            when(recordRepository.save(any(TravelRecord.class))).thenAnswer(inv -> {
                TravelRecord record = inv.getArgument(0);
                TestEntityUtils.setId(record, 1L);
                return record;
            });

            RecordResponse response = recordService.createRecord(1L, createRequestWithPlace(null, null, null));

            assertThat(response.placeName()).isNull();
            assertThat(response.latitude()).isNull();
            assertThat(response.longitude()).isNull();
        }

        @Test
        void 장소_필드가_모두_있으면_정상_생성된다() {
            when(recordRepository.save(any(TravelRecord.class))).thenAnswer(inv -> {
                TravelRecord record = inv.getArgument(0);
                TestEntityUtils.setId(record, 1L);
                return record;
            });

            RecordResponse response = recordService.createRecord(1L,
                    createRequestWithPlace("부산 자갈치시장", 35.0968, 129.0306));

            assertThat(response.placeName()).isEqualTo("부산 자갈치시장");
            assertThat(response.latitude()).isEqualTo(35.0968);
        }

        @ParameterizedTest(name = "placeName={0}, latitude={1}, longitude={2} 이면 예외")
        @MethodSource("duribun.be.domain.record.service.RecordServiceTest#partialPlaceCombinations")
        void 장소_필드가_일부만_있으면_생성시_InvalidPlaceException을_던진다(String placeName, Double latitude, Double longitude) {
            assertThatThrownBy(() -> recordService.createRecord(1L, createRequestWithPlace(placeName, latitude, longitude)))
                    .isInstanceOf(InvalidPlaceException.class);

            verify(recordRepository, never()).save(any());
        }

        @ParameterizedTest(name = "placeName={0}, latitude={1}, longitude={2} 이면 수정시 예외")
        @MethodSource("duribun.be.domain.record.service.RecordServiceTest#partialPlaceCombinations")
        void 장소_필드가_일부만_있으면_수정시_InvalidPlaceException을_던진다(String placeName, Double latitude, Double longitude) {
            UpdateRecordRequest request = new UpdateRecordRequest(
                    null, null, null, null, placeName, latitude, longitude, null, null, null);

            assertThatThrownBy(() -> recordService.updateRecord(1L, 1L, request))
                    .isInstanceOf(InvalidPlaceException.class);

            verify(recordRepository, never()).findById(any());
        }
    }

    static Stream<Arguments> partialPlaceCombinations() {
        return Stream.of(
                Arguments.of("부산 자갈치시장", null, null),
                Arguments.of(null, 35.0968, null),
                Arguments.of(null, null, 129.0306),
                Arguments.of("부산 자갈치시장", 35.0968, null),
                Arguments.of("부산 자갈치시장", null, 129.0306),
                Arguments.of(null, 35.0968, 129.0306)
        );
    }

    // ── 내 기록 목록 조회 ───────────────────────────────────────────────────────

    @Nested
    class 내_기록_목록_조회 {

        static Stream<Arguments> yearMonthRangeProvider() {
            return Stream.of(
                    Arguments.of(2026, 8,  LocalDate.of(2026, 8, 1),  LocalDate.of(2026, 8, 31)),
                    Arguments.of(2026, 12, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)),
                    Arguments.of(2025, 2,  LocalDate.of(2025, 2, 1),  LocalDate.of(2025, 2, 28))
            );
        }

        @Test
        void year와_month가_없으면_전체_기록을_반환한다() {
            TravelRecord r1 = recordWithId(1L, 1L);
            TravelRecord r2 = recordWithId(2L, 1L);
            when(recordRepository.findByUserIdOrderByVisitedAtAsc(1L)).thenReturn(List.of(r1, r2));
            when(recordImageRepository.findByRecordIdInOrderByRecordIdAscSortOrderAsc(any())).thenReturn(List.of());

            List<RecordSummaryResponse> result = recordService.getMyRecords(1L, null, null);

            assertThat(result).hasSize(2);
        }

        @Test
        void year만_있으면_해당_연도_전체_범위로_조회한다() {
            when(recordRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
                    1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                    .thenReturn(List.of(recordWithId(1L, 1L)));
            when(recordImageRepository.findByRecordIdInOrderByRecordIdAscSortOrderAsc(any())).thenReturn(List.of());

            List<RecordSummaryResponse> result = recordService.getMyRecords(1L, 2026, null);

            assertThat(result).hasSize(1);
            verify(recordRepository).findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
                    1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        }

        @ParameterizedTest(name = "{0}년 {1}월 → {2} ~ {3}")
        @MethodSource("yearMonthRangeProvider")
        void year와_month가_있으면_해당_월의_정확한_범위로_조회한다(
                int year, int month, LocalDate expectedFrom, LocalDate expectedTo) {
            when(recordRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
                    1L, expectedFrom, expectedTo))
                    .thenReturn(List.of());

            recordService.getMyRecords(1L, year, month);

            verify(recordRepository).findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
                    1L, expectedFrom, expectedTo);
        }

        @Test
        void 즐겨찾기한_기록이_favoritedAt_오름차순으로_먼저_온다() {
            TravelRecord notFavorited = recordWithId(1L, 1L);
            TravelRecord favoritedLater = recordWithId(2L, 1L);
            favoritedLater.toggleFavorite(LocalDateTime.of(2026, 8, 10, 0, 0));
            TravelRecord favoritedEarlier = recordWithId(3L, 1L);
            favoritedEarlier.toggleFavorite(LocalDateTime.of(2026, 8, 1, 0, 0));

            when(recordRepository.findByUserIdOrderByVisitedAtAsc(1L))
                    .thenReturn(List.of(notFavorited, favoritedLater, favoritedEarlier));
            when(recordImageRepository.findByRecordIdInOrderByRecordIdAscSortOrderAsc(any())).thenReturn(List.of());

            List<RecordSummaryResponse> result = recordService.getMyRecords(1L, null, null);

            assertThat(result).extracting(RecordSummaryResponse::id)
                    .containsExactly(3L, 2L, 1L);
        }

        @Test
        void 사진이_없는_기록은_thumbnailUrl이_null이다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findByUserIdOrderByVisitedAtAsc(1L)).thenReturn(List.of(record));
            when(recordImageRepository.findByRecordIdInOrderByRecordIdAscSortOrderAsc(any())).thenReturn(List.of());

            List<RecordSummaryResponse> result = recordService.getMyRecords(1L, null, null);

            assertThat(result.get(0).thumbnailUrl()).isNull();
        }
    }

    // ── 기록 상세 조회 ──────────────────────────────────────────────────────────

    @Nested
    class 기록_상세_조회 {

        @Test
        void 본인_기록을_조회하면_RecordResponse를_반환한다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

            RecordResponse response = recordService.getRecord(1L, 1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.weather()).isEqualTo(Weather.CLEAR);
            assertThat(response.mood()).isEqualTo(Mood.HAPPY);
        }

        @Test
        void 존재하지_않는_기록이면_RecordNotFoundException을_던진다() {
            when(recordRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recordService.getRecord(1L, 99L))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("존재하지 않는 기록입니다.");
        }

        @Test
        void 타인의_기록이면_RecordForbiddenException을_던진다() {
            TravelRecord record = recordWithId(1L, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.getRecord(1L, 1L))
                    .isInstanceOf(RecordForbiddenException.class)
                    .hasMessage("본인의 기록만 접근할 수 있습니다.");
        }
    }

    // ── 기록 수정 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_수정 {

        @Test
        void 전달된_필드는_수정되고_나머지는_기존_값을_유지한다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

            RecordResponse response = recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest("수정된 제목", null, null, null, null, null, null, null, null, null));

            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.content()).isEqualTo("내용");
            assertThat(response.placeName()).isEqualTo("부산 자갈치시장");
        }

        @Test
        void 모든_필드가_null이면_기존_값이_그대로_유지된다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

            RecordResponse response = recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest(null, null, null, null, null, null, null, null, null, null));

            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.content()).isEqualTo("내용");
        }

        @Test
        void 장소_필드를_모두_함께_보내면_장소가_갱신된다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

            RecordResponse response = recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest(null, null, null, null, "서울 남산타워", 37.5512, 126.9882, null, null, null));

            assertThat(response.placeName()).isEqualTo("서울 남산타워");
            assertThat(response.latitude()).isEqualTo(37.5512);
            assertThat(response.longitude()).isEqualTo(126.9882);
        }

        @Test
        void 사진_목록이_전달되면_기존_사진을_지우고_새로_저장한다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest(null, null, List.of("https://c.jpg"), null, null, null, null, null, null, null));

            verify(recordImageRepository).deleteByRecordId(1L);
            ArgumentCaptor<List<RecordImage>> captor = ArgumentCaptor.forClass(List.class);
            verify(recordImageRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
        }

        @Test
        void 사진_목록을_빈_배열로_보내면_사진이_모두_제거된다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest(null, null, List.of(), null, null, null, null, null, null, null));

            verify(recordImageRepository).deleteByRecordId(1L);
            verify(recordImageRepository, never()).saveAll(any());
        }

        @Test
        void 타인의_기록이면_RecordForbiddenException을_던진다() {
            TravelRecord record = recordWithId(1L, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest("수정 시도", null, null, null, null, null, null, null, null, null)))
                    .isInstanceOf(RecordForbiddenException.class)
                    .hasMessage("본인의 기록만 접근할 수 있습니다.");
        }
    }

    // ── 즐겨찾기 ───────────────────────────────────────────────────────────────

    @Nested
    class 즐겨찾기 {

        @Test
        void 즐겨찾기가_아니면_토글시_즐겨찾기_상태가_된다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());
            when(timeProvider.now()).thenReturn(LocalDateTime.of(2026, 8, 9, 12, 0));

            RecordResponse response = recordService.toggleFavorite(1L, 1L);

            assertThat(response.favorite()).isTrue();
        }

        @Test
        void 이미_즐겨찾기면_토글시_해제된다() {
            TravelRecord record = recordWithId(1L, 1L);
            record.toggleFavorite(LocalDateTime.of(2026, 8, 1, 0, 0));
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

            RecordResponse response = recordService.toggleFavorite(1L, 1L);

            assertThat(response.favorite()).isFalse();
        }

        @Test
        void 타인의_기록이면_RecordForbiddenException을_던진다() {
            TravelRecord record = recordWithId(1L, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.toggleFavorite(1L, 1L))
                    .isInstanceOf(RecordForbiddenException.class);
        }
    }

    // ── 기록 삭제 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_삭제 {

        @Test
        void 본인_기록을_삭제하면_사진도_S3와_DB에서_함께_삭제된다() {
            TravelRecord record = recordWithId(1L, 1L);
            RecordImage image = RecordImage.create(1L, "https://a.jpg", 0);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordImageRepository.findByRecordIdOrderBySortOrderAsc(1L)).thenReturn(List.of(image));

            recordService.deleteRecord(1L, 1L);

            verify(s3ImageClient).deleteImage("https://a.jpg");
            verify(recordImageRepository).deleteByRecordId(1L);
            verify(recordRepository).delete(record);
        }

        @Test
        void 타인의_기록이면_RecordForbiddenException을_던지고_삭제하지_않는다() {
            TravelRecord record = recordWithId(1L, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.deleteRecord(1L, 1L))
                    .isInstanceOf(RecordForbiddenException.class)
                    .hasMessage("본인의 기록만 접근할 수 있습니다.");

            verify(recordRepository, never()).delete(any());
        }
    }
}
