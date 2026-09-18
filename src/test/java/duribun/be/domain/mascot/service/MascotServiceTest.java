package duribun.be.domain.mascot.service;

import duribun.be.domain.mascot.entity.Mascot;
import duribun.be.domain.mascot.entity.UserMascot;
import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import duribun.be.domain.mascot.repository.MascotRepository;
import duribun.be.domain.mascot.repository.UserMascotRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MascotServiceTest {

    @Mock
    private MascotRepository mascotRepository;
    @Mock
    private UserMascotRepository userMascotRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MascotService mascotService;

    @BeforeEach
    void setUp() {
        mascotService = new MascotService(mascotRepository, userMascotRepository, eventPublisher);
    }

    private Mascot mascotWithId(Long id, Long regionId, String name) {
        Mascot mascot = Mascot.create(regionId, name, name, null);
        setId(mascot, id);
        return mascot;
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
    void handleLocationVerified_재방문이면_아무것도_하지_않는다() {
        mascotService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, false));

        verifyNoInteractions(mascotRepository);
        verifyNoInteractions(userMascotRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void handleLocationVerified_최초방문이고_매핑된_마스코트가_있으면_지급한다() {
        Mascot mascot = mascotWithId(1L, 5L, "강릉이");
        when(mascotRepository.findByRegionId(5L)).thenReturn(Optional.of(mascot));
        when(userMascotRepository.existsByUserIdAndMascotId(10L, 1L)).thenReturn(false);

        mascotService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        ArgumentCaptor<UserMascot> captor = ArgumentCaptor.forClass(UserMascot.class);
        verify(userMascotRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getMascotId()).isEqualTo(1L);
    }

    @Test
    void handleLocationVerified_최초방문이고_매핑된_마스코트가_있으면_MascotAcquiredEvent를_발행한다() {
        Mascot mascot = mascotWithId(1L, 5L, "강릉이");
        when(mascotRepository.findByRegionId(5L)).thenReturn(Optional.of(mascot));
        when(userMascotRepository.existsByUserIdAndMascotId(10L, 1L)).thenReturn(false);

        mascotService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        ArgumentCaptor<MascotAcquiredEvent> captor = ArgumentCaptor.forClass(MascotAcquiredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(10L);
        assertThat(captor.getValue().mascotId()).isEqualTo(1L);
        assertThat(captor.getValue().regionId()).isEqualTo(5L);
    }

    @Test
    void handleLocationVerified_이미_보유한_마스코트는_다시_지급하지_않고_이벤트도_발행하지_않는다() {
        Mascot mascot = mascotWithId(1L, 5L, "강릉이");
        when(mascotRepository.findByRegionId(5L)).thenReturn(Optional.of(mascot));
        when(userMascotRepository.existsByUserIdAndMascotId(10L, 1L)).thenReturn(true);

        mascotService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        verify(userMascotRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void handleLocationVerified_매핑된_마스코트가_없으면_조용히_무시하고_이벤트도_발행하지_않는다() {
        when(mascotRepository.findByRegionId(5L)).thenReturn(Optional.empty());

        mascotService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        verify(userMascotRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void getAllMascots_전체_마스코트에_보유여부를_포함해서_반환한다() {
        Mascot owned = mascotWithId(1L, 5L, "강릉이");
        Mascot notOwned = mascotWithId(2L, 6L, "서울이");
        UserMascot acquired = UserMascot.create(10L, 1L, LocalDateTime.now());
        when(mascotRepository.findAll()).thenReturn(List.of(owned, notOwned));
        when(userMascotRepository.findByUserId(10L)).thenReturn(List.of(acquired));

        var responses = mascotService.getAllMascots(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses).filteredOn(r -> r.mascotId().equals(1L))
                .allMatch(r -> r.acquired());
        assertThat(responses).filteredOn(r -> r.mascotId().equals(2L))
                .allMatch(r -> !r.acquired());
    }

    @Test
    void getMyMascots_보유한_마스코트만_반환한다() {
        Mascot mascot = mascotWithId(1L, 5L, "강릉이");
        UserMascot acquired = UserMascot.create(10L, 1L, LocalDateTime.now());
        when(userMascotRepository.findByUserId(10L)).thenReturn(List.of(acquired));
        when(mascotRepository.findAllById(List.of(1L))).thenReturn(List.of(mascot));

        var responses = mascotService.getMyMascots(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).mascotId()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("강릉이");
    }
}
