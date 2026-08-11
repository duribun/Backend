package duribun.be.domain.record.service;

import duribun.be.common.TestEntityUtils;
import duribun.be.domain.record.dto.CreateRecordRequest;
import duribun.be.domain.record.dto.RecordResponse;
import duribun.be.domain.record.dto.RecordSummaryResponse;
import duribun.be.domain.record.dto.UpdateRecordRequest;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.repository.RecordRepository;
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

    private RecordService recordService;

    @BeforeEach
    void setUp() {
        recordService = new RecordService(recordRepository);
    }

    private TravelRecord recordWithId(Long id, Long userId) {
        TravelRecord record = TravelRecord.create(
                userId, "제목", "내용", null,
                LocalDate.of(2026, 8, 9), "부산 자갈치시장"
        );
        TestEntityUtils.setId(record, id);
        return record;
    }

    // ── 기록 생성 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_생성 {

        @Test
        void 기록을_저장하고_RecordResponse를_반환한다() {
            CreateRecordRequest request = new CreateRecordRequest(
                    "부산 여행", "자갈치시장 방문", null,
                    LocalDate.of(2026, 8, 9), "부산 자갈치시장"
            );
            TravelRecord saved = recordWithId(1L, 1L);
            when(recordRepository.save(any(TravelRecord.class))).thenReturn(saved);

            RecordResponse response = recordService.createRecord(1L, request);

            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        void 저장할_기록에_요청자의_userId가_설정된다() {
            CreateRecordRequest request = new CreateRecordRequest(
                    "제주 여행", "한라산 등반", null,
                    LocalDate.of(2026, 7, 1), "한라산"
            );
            when(recordRepository.save(any(TravelRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            recordService.createRecord(42L, request);

            ArgumentCaptor<TravelRecord> captor = ArgumentCaptor.forClass(TravelRecord.class);
            verify(recordRepository).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(42L);
            assertThat(captor.getValue().getTitle()).isEqualTo("제주 여행");
        }
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

            List<RecordSummaryResponse> result = recordService.getMyRecords(1L, null, null);

            assertThat(result).hasSize(2);
        }

        @Test
        void year만_있으면_해당_연도_전체_범위로_조회한다() {
            when(recordRepository.findByUserIdAndVisitedAtBetweenOrderByVisitedAtAsc(
                    1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                    .thenReturn(List.of(recordWithId(1L, 1L)));

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
    }

    // ── 기록 상세 조회 ──────────────────────────────────────────────────────────

    @Nested
    class 기록_상세_조회 {

        @Test
        void 본인_기록을_조회하면_RecordResponse를_반환한다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            RecordResponse response = recordService.getRecord(1L, 1L);

            assertThat(response.id()).isEqualTo(1L);
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

            RecordResponse response = recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest("수정된 제목", null, null, null, null));

            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.content()).isEqualTo("내용");
            assertThat(response.placeName()).isEqualTo("부산 자갈치시장");
        }

        @Test
        void 모든_필드가_null이면_기존_값이_그대로_유지된다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            RecordResponse response = recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest(null, null, null, null, null));

            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.content()).isEqualTo("내용");
        }

        @Test
        void 타인의_기록이면_RecordForbiddenException을_던진다() {
            TravelRecord record = recordWithId(1L, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> recordService.updateRecord(1L, 1L,
                    new UpdateRecordRequest("수정 시도", null, null, null, null)))
                    .isInstanceOf(RecordForbiddenException.class)
                    .hasMessage("본인의 기록만 접근할 수 있습니다.");
        }
    }

    // ── 기록 삭제 ──────────────────────────────────────────────────────────────

    @Nested
    class 기록_삭제 {

        @Test
        void 본인_기록을_삭제한다() {
            TravelRecord record = recordWithId(1L, 1L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            recordService.deleteRecord(1L, 1L);

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
