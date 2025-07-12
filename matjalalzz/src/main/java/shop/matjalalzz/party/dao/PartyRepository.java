package shop.matjalalzz.party.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepository extends JpaRepository<Party, Long>,
    JpaSpecificationExecutor<Party> {

}
