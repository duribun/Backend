package duribun.be.domain.location.service;

import duribun.be.domain.location.dto.NewlyAcquiredMascotResponse;
import duribun.be.domain.location.dto.RegionResponse;
import duribun.be.domain.location.dto.VerifyLocationRequest;
import duribun.be.domain.location.dto.VerifyLocationResponse;
import duribun.be.domain.location.dto.VisitRecordResponse;
import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.entity.VisitRecord;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import duribun.be.domain.location.repository.RegionRepository;
import duribun.be.domain.location.repository.VisitRecordRepository;
import duribun.be.domain.location.util.DistanceCalculator;
import duribun.be.global.exception.RegionNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationVerificationService {

    private final RegionRepository regionRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LocationVerificationService(RegionRepository regionRepository,
                                        VisitRecordRepository visitRecordRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.regionRepository = regionRepository;
        this.visitRecordRepository = visitRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(RegionResponse::from)
                .toList();
    }

    public VerifyLocationResponse verify(Long userId, VerifyLocationRequest request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new RegionNotFoundException("존재하지 않는 지역입니다"));

        double distance = DistanceCalculator.calculateMeters(
                region.getLatitude(), region.getLongitude(),
                request.latitude(), request.longitude());

        if (distance > region.getVerificationRadiusMeters()) {
            return new VerifyLocationResponse(false, false, region.getId(), region.getName(), distance, List.of());
        }

        boolean isFirstVisit = visitRecordRepository.findByUserIdAndRegionId(userId, region.getId()).isEmpty();
        List<NewlyAcquiredMascotResponse> newlyAcquiredMascots = List.of();
        if (isFirstVisit) {
            try {
                visitRecordRepository.save(VisitRecord.create(userId, region.getId(), LocalDateTime.now()));
                newlyAcquiredMascots = publishAndCollectNewlyAcquiredMascots(userId, region.getId());
            } catch (DataIntegrityViolationException e) {
                // 동시에 같은 유저·지역의 방문 기록이 먼저 생성된 경우: 재방문으로 취급한다
                isFirstVisit = visitRecordRepository.findByUserIdAndRegionId(userId, region.getId()).isEmpty();
                if (isFirstVisit) {
                    throw e;
                }
            }
        }

        return new VerifyLocationResponse(true, isFirstVisit, region.getId(), region.getName(), distance, newlyAcquiredMascots);
    }

    /**
     * LocationVerifiedEvent를 발행하고, 동기 리스너(mascot 도메인) 처리가 끝난 뒤
     * 이번 호출로 새로 지급된 마스코트 정보를 응답용 DTO로 변환해 돌려준다.
     * 이벤트 리스너는 기본적으로 동기 실행되므로(별도 @Async 없음), publishEvent가
     * 반환되는 시점에는 마스코트 지급(해당된다면)까지 이미 완료된 상태다.
     */
    private List<NewlyAcquiredMascotResponse> publishAndCollectNewlyAcquiredMascots(Long userId, Long regionId) {
        LocationVerifiedEvent event = new LocationVerifiedEvent(userId, regionId, true);
        eventPublisher.publishEvent(event);
        return event.newlyAcquiredMascots().stream()
                .map(NewlyAcquiredMascotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitRecordResponse> getVisits(Long userId) {
        List<VisitRecord> visits = visitRecordRepository.findByUserId(userId);
        Map<Long, String> regionNames = regionRepository.findAllById(
                        visits.stream().map(VisitRecord::getRegionId).toList())
                .stream()
                .collect(Collectors.toMap(Region::getId, Region::getName));

        return visits.stream()
                .map(visit -> new VisitRecordResponse(
                        visit.getRegionId(), regionNames.get(visit.getRegionId()), visit.getVisitedAt()))
                .toList();
    }
}
