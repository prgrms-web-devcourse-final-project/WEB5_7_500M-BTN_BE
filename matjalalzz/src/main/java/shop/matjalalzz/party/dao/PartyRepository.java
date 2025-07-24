package shop.matjalalzz.party.dao;

import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.PartyStatus;

public interface PartyRepository extends JpaRepository<Party, Long>,
    JpaSpecificationExecutor<Party> {

    @Query("""
        select new shop.matjalalzz.party.dto.MyPartyResponse(
                p.id, p.title, s.shopName, p.metAt, p.deadline, p.status, p.maxCount, p.minCount,
                p.currentCount, p.genderCondition, p.minAge, p.maxAge, p.description, pu.isHost
        )
        from Party p
            join p.shop  s
            join p.partyUsers pu on pu.user.id = :userId
        where :cursor is null or p.id < :cursor
        order by p.id desc
        """)
    Slice<MyPartyResponse> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of);

    List<Party> findByDeadlineAfterAndStatus(LocalDateTime now, PartyStatus status);

    @Query("""
        select p
        from Party p
            join fetch p.reservation
            join fetch p.partyUsers
        where p.id = :partyId
        """)
    Optional<Party> findPartyByIdWithReservationAndPartyUsers(@Param("partyId") Long partyId);

    @Query("""
        select p
        from Party p
            join fetch p.reservation
        where p.id = :partyId
        """)
    Optional<Party> findPartyByIdWithReservation(@Param("partyId") Long partyId);

    @Query("""
        select p
        from Party p
            join fetch p.partyUsers
        where p.id = :partyId
        """)
    Optional<Party> findPartyByIdWithPartyUsers(@Param("partyId") Long partyId);
}
