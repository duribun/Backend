package duribun.be.domain.setting.service;

import duribun.be.domain.badge.repository.UserBadgeRepository;
import duribun.be.domain.badge.repository.UserMascotCounterRepository;
import duribun.be.domain.location.repository.VisitRecordRepository;
import duribun.be.domain.mascot.repository.UserMascotRepository;
import duribun.be.domain.point.repository.PointAccountRepository;
import duribun.be.domain.point.repository.PointHistoryRepository;
import duribun.be.domain.record.client.S3ImageClient;
import duribun.be.domain.record.entity.RecordImage;
import duribun.be.domain.record.entity.TravelRecord;
import duribun.be.domain.record.repository.RecordImageRepository;
import duribun.be.domain.record.repository.RecordRepository;
import duribun.be.domain.shop.repository.UserItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 탈퇴 시 계정과 연관된 모든 데이터를 하드 삭제한다(되돌릴 수 없음).
 * User row 자체는 여기서 지우지 않는다 — provider+provider_id 유니크 제약 때문에, User까지 하드 삭제하면
 * 같은 소셜 계정으로 재가입이 막힌다(UserService.withdraw()가 소프트 삭제로 row는 남겨둠). 대신 이 클래스가
 * 연관 데이터만 전부 지워서, 재로그인 시(AuthService.login()) 새 계정처럼 보이게 한다.
 * SettingService.withdraw()의 트랜잭션 범위 안에서 호출되므로, 여기서 일부 삭제가 실패하면
 * withdraw() 전체(탈퇴 상태 변경 포함)가 함께 롤백된다.
 */
@Service
public class AccountResetService {

    private final RecordRepository recordRepository;
    private final RecordImageRepository recordImageRepository;
    private final S3ImageClient s3ImageClient;
    private final UserMascotRepository userMascotRepository;
    private final UserMascotCounterRepository userMascotCounterRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final UserItemRepository userItemRepository;

    public AccountResetService(RecordRepository recordRepository,
                                RecordImageRepository recordImageRepository,
                                S3ImageClient s3ImageClient,
                                UserMascotRepository userMascotRepository,
                                UserMascotCounterRepository userMascotCounterRepository,
                                UserBadgeRepository userBadgeRepository,
                                PointAccountRepository pointAccountRepository,
                                PointHistoryRepository pointHistoryRepository,
                                VisitRecordRepository visitRecordRepository,
                                UserItemRepository userItemRepository) {
        this.recordRepository = recordRepository;
        this.recordImageRepository = recordImageRepository;
        this.s3ImageClient = s3ImageClient;
        this.userMascotRepository = userMascotRepository;
        this.userMascotCounterRepository = userMascotCounterRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.pointAccountRepository = pointAccountRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.visitRecordRepository = visitRecordRepository;
        this.userItemRepository = userItemRepository;
    }

    @Transactional
    public void resetUserData(Long userId) {
        deleteRecordsWithImages(userId);
        userMascotRepository.deleteByUserId(userId);
        userMascotCounterRepository.deleteByUserId(userId);
        userBadgeRepository.deleteByUserId(userId);
        pointAccountRepository.deleteByUserId(userId);
        pointHistoryRepository.deleteByUserId(userId);
        // 방문 인증 기록 — 여기가 안 지워지면 재가입 후 같은 지역을 다시 방문해도
        // "이미 방문한 지역"으로 처리되어 마스코트가 재지급되지 않는다.
        visitRecordRepository.deleteByUserId(userId);
        userItemRepository.deleteByUserId(userId);
    }

    /**
     * 기록은 이미지가 S3에 별도로 저장돼 있어 deleteByUserId 파생 쿼리만으로는 부족하다.
     * RecordService.deleteRecord()와 동일한 순서(S3 이미지 삭제 → RecordImage 삭제 → TravelRecord 삭제)로 처리한다.
     */
    private void deleteRecordsWithImages(Long userId) {
        List<TravelRecord> records = recordRepository.findByUserIdOrderByVisitedAtAsc(userId);
        for (TravelRecord record : records) {
            List<RecordImage> images = recordImageRepository.findByRecordIdOrderBySortOrderAsc(record.getId());
            images.forEach(image -> s3ImageClient.deleteImage(image.getImageUrl()));
            recordImageRepository.deleteByRecordId(record.getId());
        }
        recordRepository.deleteAll(records);
    }
}
