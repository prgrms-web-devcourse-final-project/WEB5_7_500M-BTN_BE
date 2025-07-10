package shop.matjalalzz.party.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepository extends JpaRepository<Party, Long> {

}
