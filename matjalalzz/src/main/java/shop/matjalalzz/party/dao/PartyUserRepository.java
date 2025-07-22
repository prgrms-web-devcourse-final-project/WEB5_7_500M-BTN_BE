package shop.matjalalzz.party.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.party.entity.PartyUser;

public interface PartyUserRepository extends JpaRepository<PartyUser, Long> {

    Optional<PartyUser> findByUserIdAndPartyId(Long userId, Long partyId);

    List<PartyUser> findAllByPartyId(Long partyId);

    boolean existsByUserIdAndPartyId(Long userId, Long partyId);
}
