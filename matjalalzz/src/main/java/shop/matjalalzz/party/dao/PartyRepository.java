package shop.matjalalzz.party.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.PartyStatus;

public interface PartyRepository extends JpaRepository<Party, Long>,
    JpaSpecificationExecutor<Party> {

    @Query("""
        select p.id as partyId, p.title, s.shopName, p.metAt, p.deadline, p.status, p.maxCount, p.minCount,
               p.currentCount, p.genderCondition, p.minAge, p.maxAge, p.description, pu.isHost
        from Party p
            join p.shop  s
            join p.partyUsers pu on pu.user.id = :userId
        where :cursor is null or p.id < :cursor
        order by p.id desc
        """)
    <T> Slice<T> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of);

    List<Party> findByDeadlineAfterAndStatus(LocalDateTime now, PartyStatus status);

    @Query("""
        select p
        from Party p
            join fetch p.partyUsers
        where p.id = :partyId
        """)
    Optional<Party> findPartyByIdWithPartyUsers(@Param("partyId") Long partyId);

    @Query("""
        SELECT p
        FROM Party p
            JOIN p.partyUsers pu ON
                pu.user.id = :userId
                AND pu.isHost
            LEFT JOIN p.reservation r
        WHERE p.status <> "TERMINATED"
            AND (
                r IS NULL
                OR r.status = "PENDING"
                OR (
                    r.status = "CONFIRMED"
                    AND r.reservedAt >= :threshold
                )
            )
        """)
    List<Party> findAllMyPartyByUserIdForWithdraw(@Param("userId") long userId,
        @Param("threshold") LocalDateTime threshold);

    @Query("""
        SELECT p
        FROM Party p
            JOIN p.partyUsers pu ON
                pu.user.id = :userId
                AND pu.isHost = false
            LEFT JOIN p.reservation r
        WHERE p.status <> "TERMINATED"
            AND (
                r IS NULL
                OR r.status = "PENDING"
            )
        """)
    List<Party> findAllParticipatingPartyByUserIdForWithdraw(@Param("userId") long userId);
}
