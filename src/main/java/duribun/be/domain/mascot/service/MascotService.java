package duribun.be.domain.mascot.service;

import duribun.be.domain.mascot.dto.MascotResponse;
import duribun.be.domain.mascot.dto.MyMascotResponse;
import duribun.be.domain.mascot.entity.Mascot;
import duribun.be.domain.mascot.entity.UserMascot;
import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import duribun.be.domain.mascot.repository.MascotRepository;
import duribun.be.domain.mascot.repository.UserMascotRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import duribun.be.domain.location.event.NewlyAcquiredMascotInfo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class MascotService {

    private final MascotRepository mascotRepository;
    private final UserMascotRepository userMascotRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MascotService(MascotRepository mascotRepository,
                          UserMascotRepository userMascotRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.mascotRepository = mascotRepository;
        this.userMascotRepository = userMascotRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleLocationVerified(LocationVerifiedEvent event) {
        if (!event.isFirstVisit()) {
            return;
        }

        mascotRepository.findByRegionId(event.regionId())
                .filter(mascot -> !userMascotRepository.existsByUserIdAndMascotId(event.userId(), mascot.getId()))
                .ifPresent(mascot -> {
                    grantMascot(event.userId(), mascot.getId());
                    event.addNewlyAcquiredMascot(
                            new NewlyAcquiredMascotInfo(mascot.getId(), mascot.getName(), mascot.getImageUrl()));
                    eventPublisher.publishEvent(
                            new MascotAcquiredEvent(event.userId(), mascot.getId(), event.regionId()));
                });
    }

    public List<MascotResponse> getAllMascots(Long userId) {
        List<Mascot> mascots = mascotRepository.findAll();
        Map<Long, UserMascot> acquiredByMascotId = userMascotRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserMascot::getMascotId, Function.identity()));

        return mascots.stream()
                .map(mascot -> MascotResponse.of(mascot, acquiredByMascotId.get(mascot.getId())))
                .toList();
    }

    public List<MyMascotResponse> getMyMascots(Long userId) {
        List<UserMascot> userMascots = userMascotRepository.findByUserId(userId);
        Map<Long, Mascot> mascotsById = mascotRepository
                .findAllById(userMascots.stream().map(UserMascot::getMascotId).toList())
                .stream()
                .collect(Collectors.toMap(Mascot::getId, Function.identity()));

        return userMascots.stream()
                .map(userMascot -> MyMascotResponse.of(mascotsById.get(userMascot.getMascotId()), userMascot))
                .toList();
    }

    private void grantMascot(Long userId, Long mascotId) {
        try {
            userMascotRepository.save(UserMascot.create(userId, mascotId, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            // 동시 최초 지급 경합: 다른 트랜잭션이 이미 동일 마스코트를 지급했으므로 무시한다
        }
    }
}
