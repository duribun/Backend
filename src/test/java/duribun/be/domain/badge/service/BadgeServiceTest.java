package duribun.be.domain.badge.service;

import duribun.be.domain.badge.entity.Badge;
import duribun.be.domain.badge.entity.UserBadge;
import duribun.be.domain.badge.entity.UserVisitCounter;
import duribun.be.domain.badge.repository.BadgeRepository;
import duribun.be.domain.badge.repository.UserBadgeRepository;
import duribun.be.domain.badge.repository.UserVisitCounterRepository;
import duribun.be.domain.location.event.LocationVerifiedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private UserBadgeRepository userBadgeRepository;
    @Mock
    private UserVisitCounterRepository userVisitCounterRepository;

    private BadgeService badgeService;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeService(badgeRepository, userBadgeRepository, userVisitCounterRepository);
    }

    private Badge badgeWithId(Long id, String code, String name, int requiredVisitCount) {
        Badge badge = Badge.create(code, name, name, requiredVisitCount, null);
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
    void handleLocationVerified_재방문이면_카운터가_증가하지_않는다() {
        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, false));

        verify(userVisitCounterRepository, never()).findByUserId(any());
        verify(userVisitCounterRepository, never()).save(any());
        verify(badgeRepository, never()).findByRequiredVisitCountLessThanEqual(any());
    }

    @Test
    void handleLocationVerified_최초방문이면_카운터가_없을때_새로_생성해서_1로_증가시킨다() {
        when(userVisitCounterRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(badgeRepository.findByRequiredVisitCountLessThanEqual(1)).thenReturn(List.of());

        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, true));

        ArgumentCaptor<UserVisitCounter> captor = ArgumentCaptor.forClass(UserVisitCounter.class);
        verify(userVisitCounterRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getVisitCount()).isEqualTo(1);
        verify(badgeRepository).findByRequiredVisitCountLessThanEqual(1);
    }

    @Test
    void handleLocationVerified_기존_카운터가_있으면_1_증가시킨다() {
        UserVisitCounter counter = UserVisitCounter.create(10L);
        counter.increase();
        counter.increase();
        when(userVisitCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredVisitCountLessThanEqual(3)).thenReturn(List.of());

        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        ArgumentCaptor<UserVisitCounter> captor = ArgumentCaptor.forClass(UserVisitCounter.class);
        verify(userVisitCounterRepository).save(captor.capture());
        assertThat(captor.getValue().getVisitCount()).isEqualTo(3);
    }

    @Test
    void handleLocationVerified_기준을_충족하고_미보유_배지면_UserBadge를_저장한다() {
        UserVisitCounter counter = UserVisitCounter.create(10L);
        Badge beginner = badgeWithId(1L, "BEGINNER", "여행 초보자", 1);
        when(userVisitCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredVisitCountLessThanEqual(1)).thenReturn(List.of(beginner));
        when(userBadgeRepository.existsByUserIdAndBadgeId(10L, 1L)).thenReturn(false);

        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, true));

        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadgeRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getBadgeId()).isEqualTo(1L);
    }

    @Test
    void handleLocationVerified_이미_보유한_배지는_다시_부여하지_않는다() {
        UserVisitCounter counter = UserVisitCounter.create(10L);
        Badge beginner = badgeWithId(1L, "BEGINNER", "여행 초보자", 1);
        when(userVisitCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredVisitCountLessThanEqual(1)).thenReturn(List.of(beginner));
        when(userBadgeRepository.existsByUserIdAndBadgeId(10L, 1L)).thenReturn(true);

        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 1L, true));

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void handleLocationVerified_한번에_여러_배지_기준을_넘으면_모두_부여한다() {
        UserVisitCounter counter = UserVisitCounter.create(10L);
        for (int i = 0; i < 4; i++) {
            counter.increase();
        }
        Badge beginner = badgeWithId(1L, "BEGINNER", "여행 초보자", 1);
        Badge explorer = badgeWithId(2L, "EXPLORER", "국내 탐험가", 5);
        when(userVisitCounterRepository.findByUserId(10L)).thenReturn(Optional.of(counter));
        when(badgeRepository.findByRequiredVisitCountLessThanEqual(5)).thenReturn(List.of(beginner, explorer));
        when(userBadgeRepository.existsByUserIdAndBadgeId(eq(10L), anyLong())).thenReturn(false);

        badgeService.handleLocationVerified(new LocationVerifiedEvent(10L, 5L, true));

        verify(userBadgeRepository, times(2)).save(any(UserBadge.class));
    }

    @Test
    void getAllBadges_전체_배지에_유저의_획득여부를_포함해서_반환한다() {
        Badge beginner = badgeWithId(1L, "BEGINNER", "여행 초보자", 1);
        Badge explorer = badgeWithId(2L, "EXPLORER", "국내 탐험가", 5);
        UserBadge acquired = UserBadge.create(10L, 1L, java.time.LocalDateTime.now());
        when(badgeRepository.findAll()).thenReturn(List.of(beginner, explorer));
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquired));

        var responses = badgeService.getAllBadges(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses).filteredOn(r -> r.code().equals("BEGINNER"))
                .allMatch(r -> r.acquired());
        assertThat(responses).filteredOn(r -> r.code().equals("EXPLORER"))
                .allMatch(r -> !r.acquired());
    }

    @Test
    void getMyBadges_보유한_배지만_반환한다() {
        Badge beginner = badgeWithId(1L, "BEGINNER", "여행 초보자", 1);
        UserBadge acquired = UserBadge.create(10L, 1L, java.time.LocalDateTime.now());
        when(userBadgeRepository.findByUserId(10L)).thenReturn(List.of(acquired));
        when(badgeRepository.findAllById(List.of(1L))).thenReturn(List.of(beginner));

        var responses = badgeService.getMyBadges(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).code()).isEqualTo("BEGINNER");
    }
}
