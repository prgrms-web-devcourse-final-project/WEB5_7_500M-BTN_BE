package shop.matjalalzz.party.app;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Service
@Slf4j
public class PartySchedulerService {

    private final TaskScheduler taskScheduler;
    private final PartyStatusService partyStatusService;
    private final PartyRepository partyRepository;

    public PartySchedulerService(
        @Qualifier("customTaskScheduler") TaskScheduler taskScheduler,
        PartyStatusService partyStatusService,
        PartyRepository partyRepository) {
        this.taskScheduler = taskScheduler;
        this.partyStatusService = partyStatusService;
        this.partyRepository = partyRepository;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleInit() {
        List<Party> parties = partyRepository.findByDeadlineAfterAndStatus(
            LocalDateTime.now(), PartyStatus.RECRUITING);

        for (Party party : parties) {
            scheduleDeadlineJob(party);
        }
        log.info("파티 상태 변경 스케줄 task 초기화 완료");
    }

    public void scheduleDeadlineJob(Party party) {
        LocalDateTime deadline = party.getDeadline();
//        Date triggerTime = Date.from(deadline.atZone(ZoneId.systemDefault()).toInstant());
        Instant instant = deadline.atZone(ZoneId.systemDefault()).toInstant();

        // 새 스레드에서 스케줄 실행 -> 새 스레드에서 적용할 트랜잭션 필요
        taskScheduler.schedule(() -> partyStatusService.updateStatusByDeadline(party.getId()),
            instant);

    }

}
