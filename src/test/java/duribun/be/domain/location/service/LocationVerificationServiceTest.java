package duribun.be.domain.location.service;

import duribun.be.domain.location.dto.VerifyLocationRequest;
import duribun.be.domain.location.dto.VerifyLocationResponse;
import duribun.be.domain.location.dto.VisitRecordResponse;
import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.entity.VisitRecord;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.location.repository.VisitRecordRepository;
import duribun.be.global.exception.RegionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationVerificationServiceTest {

    @Mock
    private RegionRepository regionRepository;
    @Mock
    private VisitRecordRepository visitRecordRepository;

    private ApplicationEventPublisher eventPublisher;
    private LocationVerificationService locationVerificationService;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        locationVerificationService = new LocationVerificationService(regionRepository, visitRecordRepository, eventPublisher);
    }

    private Region regionWithId(Long id, String name, double lat, double lon, int radius) {
        Region region = Region.create("51150", name, lat, lon, radius);
        setId(region, id);
        return region;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void verify_반경_이내_좌표면_verified_true이고_VisitRecord를_생성한다() {
        Region region = regionWithId(1L, "강릉시", 37.7519, 128.8761, 1000);
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(visitRecordRepository.findByUserIdAndRegionId(10L, 1L)).thenReturn(Optional.empty());

        VerifyLocationResponse response = locationVerificationService.verify(
                10L, new VerifyLocationRequest(1L, 37.7519, 128.8761));

        assertThat(response.verified()).isTrue();
        assertThat(response.isFirstVisit()).isTrue();
        assertThat(response.regionId()).isEqualTo(1L);
        assertThat(response.regionName()).isEqualTo("강릉시");
        verify(visitRecordRepository).save(any(VisitRecord.class));
        verify(eventPublisher).publishEvent(new LocationVerifiedEvent(10L, 1L, true));
    }

    @Test
    void verify_반경_밖_좌표면_verified_false이고_VisitRecord를_생성하지_않는다() {
        Region region = regionWithId(1L, "강릉시", 37.7519, 128.8761, 1000);
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        VerifyLocationResponse response = locationVerificationService.verify(
                10L, new VerifyLocationRequest(1L, 38.7519, 129.8761));

        assertThat(response.verified()).isFalse();
        assertThat(response.isFirstVisit()).isFalse();
        assertThat(response.distanceMeters()).isGreaterThan(1000);
        verify(visitRecordRepository, never()).save(any(VisitRecord.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void verify_이미_방문한_지역이면_중복_레코드_없이_isFirstVisit_false를_반환한다() {
        Region region = regionWithId(1L, "강릉시", 37.7519, 128.8761, 1000);
        VisitRecord existing = VisitRecord.create(10L, 1L, LocalDateTime.now());
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(visitRecordRepository.findByUserIdAndRegionId(10L, 1L)).thenReturn(Optional.of(existing));

        VerifyLocationResponse response = locationVerificationService.verify(
                10L, new VerifyLocationRequest(1L, 37.7519, 128.8761));

        assertThat(response.verified()).isTrue();
        assertThat(response.isFirstVisit()).isFalse();
        verify(visitRecordRepository, never()).save(any(VisitRecord.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void verify_존재하지_않는_regionId면_RegionNotFoundException을_던진다() {
        when(regionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationVerificationService.verify(
                10L, new VerifyLocationRequest(999L, 37.7519, 128.8761)))
                .isInstanceOf(RegionNotFoundException.class);
    }

    @Test
    void getVisits_로그인한_유저의_방문_목록을_지역명과_함께_반환한다() {
        Region region = regionWithId(1L, "강릉시", 37.7519, 128.8761, 1000);
        LocalDateTime visitedAt = LocalDateTime.now();
        VisitRecord visitRecord = VisitRecord.create(10L, 1L, visitedAt);
        when(visitRecordRepository.findByUserId(10L)).thenReturn(List.of(visitRecord));
        when(regionRepository.findAllById(List.of(1L))).thenReturn(List.of(region));

        List<VisitRecordResponse> visits = locationVerificationService.getVisits(10L);

        assertThat(visits).hasSize(1);
        assertThat(visits.get(0).regionId()).isEqualTo(1L);
        assertThat(visits.get(0).regionName()).isEqualTo("강릉시");
        assertThat(visits.get(0).visitedAt()).isEqualTo(visitedAt);
    }
}
