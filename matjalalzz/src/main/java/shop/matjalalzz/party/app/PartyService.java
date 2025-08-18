package shop.matjalalzz.party.app;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.app.PartyChatService;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyQueryDslRepository;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.dto.PartyMemberResponse;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.dto.projection.MyPartyProjection;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.party.mapper.projection.ProjectionToDtoMapper;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyChatService partyChatService;
    private final PartyRepository partyRepository;
    private final PartyUserRepository partyUserRepository;
    private final PartyQueryDslRepository partyQueryDslRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public void saveParty(Party party) {
        partyRepository.save(party);
    }

    @Transactional
    public void breakParty(Party party) {
        // 삭제됐거나 종료된 상태의 파티는 삭제 불가
        if (party.isDeleted() || party.getStatus() == PartyStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY_TERMINATED);
        }

        User host = findPartyHost(party);
        party.deleteParty();
        partyChatService.noticePartyDeleted(host, party);
    }

    @Transactional
    public void terminateParty(Party party) {
        User host = findPartyHost(party);
        partyChatService.noticePartyDeleted(host, party);
        party.terminate();
    }

    @Transactional(readOnly = true)
    public Optional<Boolean> findByUserIdAndPartyId(Long userId, Long partyId) {
        return partyUserRepository.findByUserIdAndPartyId(userId, partyId);
    }

    @Transactional(readOnly = true)
    public Slice<MyPartyResponse> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of) {
        Slice<MyPartyProjection> projections = partyRepository.findByUserIdAndCursor(userId,
            cursor, of);

        return projections.map(ProjectionToDtoMapper::toMyPartyResponse);
    }

    @Transactional(readOnly = true)
    public List<Party> searchParties(PartySearchParam cond, int size) {
        return partyQueryDslRepository.searchWithCursor(cond, size);
    }

    @Transactional(readOnly = true)
    public List<PartyMemberResponse> findAllByPartyIdToDto(long partyId) {
        return partyUserRepository.findMembersByPartyId(partyId, BASE_URL).stream()
            .map(ProjectionToDtoMapper::toPartyMemberResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Party findByIdWithPartyUsers(Long partyId) {
        return partyRepository.findPartyByIdWithPartyUsers(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PartyUser findPartyUser(long userId, Party party) {
        return party.getPartyUsers().stream()
            .filter(pu -> pu.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOIN_PARTY));
    }

    @Transactional(readOnly = true)
    public List<Party> findAllMyRecruitingParty(long userId) {
        return partyRepository.findAllMyRecruitingParty(userId);
    }

    @Transactional(readOnly = true)
    public List<Party> findAllParticipatingParty(long userId) {
        return partyRepository.findAllParticipatingParty(userId);
    }

    @Transactional(readOnly = true)
    public User findPartyHost(Party party) {
        return party.getPartyUsers().stream()
            .filter(PartyUser::isHost)
            .findFirst()
            .map(PartyUser::getUser)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Party findById(Long partyId) {
        return partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PartyUser> getPartyUsers(Long partyId) {
        return partyUserRepository.findAllByPartyIdWithUser(partyId);
    }

    @Transactional(readOnly = true)
    public boolean isInParty(Long partyId, Long userId) {
        return partyUserRepository.existsByUserIdAndPartyId(userId, partyId);
    }

    @Transactional(readOnly = true)
    public Party findByIdWithShop(Long partyId) {
        return partyRepository.findByIdWithShop(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }
}
