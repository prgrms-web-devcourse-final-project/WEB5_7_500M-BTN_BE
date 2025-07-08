package shop.matjalalzz.domain.party.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.domain.party.entity.Party;

public interface PartyRepository extends JpaRepository<Party, Long> {
}
