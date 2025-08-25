package shop.matjalalzz.party.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.dto.projection.MyPartyProjection;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.PartyStatus;

public interface PartyRepository extends JpaRepository<Party, Long>, PartyRepositoryCustom {

    @Query("""
        SELECT
            p.id as id,
            p.title as title,
            s.shopName as shopName,
            p.metAt as metAt,
            p.deadline as deadline,
            p.status as status,
            p.maxCount as maxCount,
            p.minCount as minCount,
            p.currentCount as currentCount,
            p.genderCondition as genderCondition,
            p.minAge as minAge,
            p.maxAge as maxAge,
            p.description as description,
            pu.isHost as isHost
        FROM PartyUser pu
            JOIN pu.party p
            JOIN p.shop s
        WHERE pu.user.id = :userId
            AND (:cursor is null or pu.party.id < :cursor)
        ORDER BY pu.party.id desc
        """)
    Slice<MyPartyProjection> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of);

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
        FROM PartyUser pu
            JOIN pu.party p
        WHERE pu.user.id = :userId
            AND pu.isHost
            AND p.status <> "TERMINATED"
        """)
    List<Party> findAllMyRecruitingParty(@Param("userId") long userId);

    @Query("""
        SELECT pu
        FROM PartyUser pu
            JOIN FETCH pu.party p
        WHERE pu.user.id = :userId
            AND pu.isHost = false
            AND p.status <> "TERMINATED"
        """)
    List<PartyUser> findAllParticipatingParty(@Param("userId") long userId);

    @Query("""
            SELECT p
            FROM Party p
                JOIN FETCH p.shop
            WHERE p.id=:partyId
        """)
    Optional<Party> findByIdWithShop(@Param("partyId") Long partyId);
}
