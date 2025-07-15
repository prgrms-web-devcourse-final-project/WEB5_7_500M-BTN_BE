package shop.matjalalzz.party.app;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;

@RequiredArgsConstructor
@Service
public class PartyScheduler {

    private final PartyRepository partyRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") //매일 자정
//    @Scheduled(fixedDelay = 30000) //30초마다 실행(테스트용)
    public void updatePartyStatus() {
        List<Party> parties = partyRepository.findPartiesDeadlineToday(LocalDate.now());

        for (Party party : parties) {
            if (party.getCurrentCount() >= party.getMinCount()) {
                party.complete();
            } else {
                party.deleteParty(); //TODO: 예약금 환불 로직 필요
            }
        }
    }
}
