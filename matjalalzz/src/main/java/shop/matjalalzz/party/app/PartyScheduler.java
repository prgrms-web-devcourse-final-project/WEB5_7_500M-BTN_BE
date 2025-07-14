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
    public void updatePartyStatus() {
        LocalDate today = LocalDate.now();
        List<Party> parties = partyRepository.findPartiesDeadlineToday(today);

        for (Party party : parties) {
            if (party.getCurrentCount() >= party.getMinCount()) {
                party.complete();
            } else {
                party.cancel(); //TODO: 예약금 환불 로직 필요
            }
        }
    }

}
