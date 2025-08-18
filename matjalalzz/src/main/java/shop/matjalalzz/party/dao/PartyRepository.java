package shop.matjalalzz.party.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.PartyStatus;

public interface PartyRepository extends JpaRepository<Party, Long> {

    @Query("""
        select new shop.matjalalzz.party.dto.MyPartyResponse(
                p.id, p.title, s.shopName, p.metAt, p.deadline, p.status, p.maxCount, p.minCount,
                p.currentCount, p.genderCondition, p.minAge, p.maxAge, p.description, pu.isHost
        )
        from PartyUser pu
            join pu.party p
            join p.shop s
        where pu.user.id = :userId
            and (:cursor is null or pu.party.id < :cursor)
        order by p.id desc
        """)
    Slice<MyPartyResponse> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of);

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

    @Query("""
            SELECT p
            FROM Party p
                JOIN FETCH p.shop
            WHERE p.id=:partyId
        """)
    Optional<Party> findByIdWithShop(@Param("partyId") Long partyId);
}
