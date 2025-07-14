package shop.matjalalzz.party.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepository extends JpaRepository<Party, Long>,
    JpaSpecificationExecutor<Party> {

    @Query("SELECT p FROM Party p WHERE p.deadline = :today AND p.status = 'RECRUITING'")
    List<Party> findPartiesDeadlineToday(@Param("today") LocalDate today);

}
