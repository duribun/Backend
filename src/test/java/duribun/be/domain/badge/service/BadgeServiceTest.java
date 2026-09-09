package duribun.be.domain.badge.service;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;
import duribun.be.domain.badge.entity.UserMascotCounter;
import duribun.be.domain.badge.event.BadgeAcquiredEvent;
import duribun.be.domain.badge.repository.BadgeRepository;
import duribun.be.domain.badge.repository.UserBadgeRepository;
import duribun.be.domain.badge.repository.UserMascotCounterRepository;
import duribun.be.domain.mascot.event.MascotAcquiredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private UserBadgeRepository userBadgeRepository;
    @Mock
    private UserMascotCounterRepository userMascotCounterRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BadgeService badgeService;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeService(badgeRepository, userBadgeRepository, userMascotCounterRepository, eventPublisher);
    }

    private Badge badgeWithId(Long id, String code, String name, int requiredMascotCount) {
        Badge badge = Badge.create(code, name, name, requiredMascotCount, null);
        setId(badge, id);
        return badge;
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
    void handleMascotAcquired_카운터가_없을때_새로_생성해서_1로_증가시킨다() {
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(1)).thenReturn(List.of());

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 1L, 1L));

        ArgumentCaptor<UserMascotCounter> captor = ArgumentCaptor.forClass(UserMascotCounter.class);
        verify(userMascotCounterRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getMascotCount()).isEqualTo(1);
        verify(badgeRepository).findByRequiredMascotCountLessThanEqual(1);
    }

    @Test
    void handleMascotAcquired_기존_카운터가_있으면_1_증가시킨다() {
        UserMascotCounter counter = UserMascotCounter.create(10L);
        counter.increase();
        counter.increase();
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(3)).thenReturn(List.of());

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 2L, 5L));

        ArgumentCaptor<UserMascotCounter> captor = ArgumentCaptor.forClass(UserMascotCounter.class);
        verify(userMascotCounterRepository).save(captor.capture());
        assertThat(captor.getValue().getMascotCount()).isEqualTo(3);
    }

    @Test
    void handleMascotAcquired_기준을_충족하고_미보유_배지면_UserBadge를_저장한다() {
        UserMascotCounter counter = UserMascotCounter.create(10L);
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(1)).thenReturn(List.of(seedling));
        when(userBadgeRepository.existsByUserIdAndBadgeId(10L, 1L)).thenReturn(false);

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 1L, 1L));

        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadgeRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getBadgeId()).isEqualTo(1L);
    }

    @Test
    void handleMascotAcquired_이미_보유한_배지는_다시_부여하지_않고_이벤트도_발행하지_않는다() {
        UserMascotCounter counter = UserMascotCounter.create(10L);
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(1)).thenReturn(List.of(seedling));
        when(userBadgeRepository.existsByUserIdAndBadgeId(10L, 1L)).thenReturn(true);

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 1L, 1L));

        verify(userBadgeRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void handleMascotAcquired_한번에_여러_배지_기준을_넘으면_모두_부여하고_각각_이벤트를_발행한다() {
        UserMascotCounter counter = UserMascotCounter.create(10L);
        for (int i = 0; i < 4; i++) {
            counter.increase();
        }
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        Badge beginner = badgeWithId(2L, "BEGINNER", "여행 입문자", 5);
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(5)).thenReturn(List.of(seedling, beginner));
        when(userBadgeRepository.existsByUserIdAndBadgeId(eq(10L), anyLong())).thenReturn(false);

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 3L, 5L));

        verify(userBadgeRepository, times(2)).save(any(UserBadge.class));

        ArgumentCaptor<BadgeAcquiredEvent> captor = ArgumentCaptor.forClass(BadgeAcquiredEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(BadgeAcquiredEvent::badgeId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(captor.getAllValues())
                .extracting(BadgeAcquiredEvent::badgeCode)
                .containsExactlyInAnyOrder("SEEDLING", "BEGINNER");
    }

    @Test
    void handleMascotAcquired_기준을_충족하고_미보유_배지면_BadgeAcquiredEvent를_발행한다() {
        UserMascotCounter counter = UserMascotCounter.create(10L);
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        when(userMascotCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredMascotCountLessThanEqual(1)).thenReturn(List.of(seedling));
        when(userBadgeRepository.existsByUserIdAndBadgeId(10L, 1L)).thenReturn(false);

        badgeService.handleMascotAcquired(new MascotAcquiredEvent(10L, 1L, 1L));

        ArgumentCaptor<BadgeAcquiredEvent> captor = ArgumentCaptor.forClass(BadgeAcquiredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(10L);
        assertThat(captor.getValue().badgeId()).isEqualTo(1L);
        assertThat(captor.getValue().badgeCode()).isEqualTo("SEEDLING");
    }

    @Test
    void getAllBadges_전체_배지에_유저의_획득여부를_포함해서_반환한다() {
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        Badge beginner = badgeWithId(2L, "BEGINNER", "여행 입문자", 5);
        UserBadge acquired = UserBadge.create(10L, 1L, java.time.LocalDateTime.now());
        when(badgeRepository.findAll()).thenReturn(List.of(seedling, beginner));
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquired));

        var responses = badgeService.getAllBadges(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses).filteredOn(r -> r.code().equals("SEEDLING"))
                .allMatch(r -> r.acquired());
        assertThat(responses).filteredOn(r -> r.code().equals("BEGINNER"))
                .allMatch(r -> !r.acquired());
    }

    @Test
    void getMyBadges_보유한_배지만_반환한다() {
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        UserBadge acquired = UserBadge.create(10L, 1L, java.time.LocalDateTime.now());
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquired));
        when(badgeRepository.findAllById(List.of(1L))).thenReturn(List.of(seedling));

        var responses = badgeService.getMyBadges(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).code()).isEqualTo("SEEDLING");
    }

    @Test
    void getMyBadges_응답에_requiredMascotCount가_포함된다() {
        Badge beginner = badgeWithId(2L, "BEGINNER", "여행 입문자", 5);
        UserBadge acquired = UserBadge.create(10L, 2L, java.time.LocalDateTime.now());
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquired));
        when(badgeRepository.findAllById(List.of(2L))).thenReturn(List.of(beginner));

        var responses = badgeService.getMyBadges(10L);

        assertThat(responses.get(0).requiredMascotCount()).isEqualTo(5);
    }

    @Test
    void getMyBadges_requiredMascotCount_내림차순으로_정렬한다() {
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        Badge beginner = badgeWithId(2L, "BEGINNER", "여행 입문자", 5);
        Badge explorer = badgeWithId(3L, "EXPLORER", "여행 탐험가", 10);
        UserBadge acquiredSeedling = UserBadge.create(10L, 1L, java.time.LocalDateTime.now());
        UserBadge acquiredBeginner = UserBadge.create(10L, 2L, java.time.LocalDateTime.now());
        UserBadge acquiredExplorer = UserBadge.create(10L, 3L, java.time.LocalDateTime.now());
        when(userBadgeRepository.findByUserId(10L))
                .thenReturn(List.of(acquiredSeedling, acquiredBeginner, acquiredExplorer));
        when(badgeRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(seedling, beginner, explorer));

        var responses = badgeService.getMyBadges(10L);

        assertThat(responses).extracting("code").containsExactly("EXPLORER", "BEGINNER", "SEEDLING");
    }

    @Test
    void getMyBadges_한번에_여러_배지를_동시에_획득해도_requiredMascotCount로_최고단계를_가려낼_수_있다() {
        java.time.LocalDateTime sameInstant = java.time.LocalDateTime.of(2026, 8, 1, 0, 0);
        Badge seedling = badgeWithId(1L, "SEEDLING", "여행 새싹", 0);
        Badge beginner = badgeWithId(2L, "BEGINNER", "여행 입문자", 5);
        UserBadge acquiredSeedling = UserBadge.create(10L, 1L, sameInstant);
        UserBadge acquiredBeginner = UserBadge.create(10L, 2L, sameInstant);
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquiredSeedling, acquiredBeginner));
        when(badgeRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(seedling, beginner));

        var responses = badgeService.getMyBadges(10L);

        assertThat(responses.get(0).code()).isEqualTo("BEGINNER");
    }
}
