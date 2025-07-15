package shop.matjalalzz.party.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyStatusService {

    private final PartyRepository partyRepository;

    @Transactional
    public void updateStatusByDeadline(Long partyId) {

        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        if (!party.getStatus().equals(PartyStatus.RECRUITING)) {
            return;
        }

        if (party.getCurrentCount() >= party.getMinCount()) {
            party.complete();
        } else {
            party.deleteParty();
        }
        log.info("파티 상태 변경 완료");
    }

}
