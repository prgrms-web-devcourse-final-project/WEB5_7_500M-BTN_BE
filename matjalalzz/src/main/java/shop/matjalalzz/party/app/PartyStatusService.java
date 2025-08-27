package shop.matjalalzz.party.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.app.PartyChatService;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartyStatusService {

    private final PartyRepository partyRepository;
    private final PartyService partyService;
    private final PartyChatService partyChatService;

    @Transactional
    public void updateStatusByDeadline(Long partyId) {

        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PARTY_NOT_FOUND));

        if (!party.isRecruiting()) {
            return;
        }

        User host = partyService.findPartyHost(party);

        if (party.getCurrentCount() >= party.getMinCount()) {
            party.complete();
            partyChatService.noticePaymentRequest(host, party);
        } else {
            party.deleteParty();
            partyChatService.noticePartyDeleted(host, party);
        }

        log.info("파티 상태 변경 완료");
    }

}
