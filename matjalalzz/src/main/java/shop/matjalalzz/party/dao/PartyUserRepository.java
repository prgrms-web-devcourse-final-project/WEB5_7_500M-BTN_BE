package shop.matjalalzz.party.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.dto.PartyMemberResponse;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.user.entity.User;

public interface PartyUserRepository extends JpaRepository<PartyUser, Long> {

    @Query(value = """
        select pu.* from party_user pu
        where pu.user_id = :userId and pu.party_id = :partyId
        """, nativeQuery = true)
    Optional<PartyUser> findByUserIdAndPartyId(@Param("userId") Long userId,
        @Param("partyId") Long partyId);

    List<PartyUser> findAllByPartyId(Long partyId);

    @Query(value = """
        select new shop.matjalalzz.party.dto.PartyMemberResponse(
            u.id, u.nickname, concat(:baseUrl, u.profileKey), pu.isHost
        )
        from PartyUser pu
            join pu.user u
        where pu.party.id = :partyId
        """)
    List<PartyMemberResponse> findAllByPartyIdToDto(@Param("partyId") long partyId,
        @Param("baseUrl") String baseUrl);

    boolean existsByUserIdAndPartyId(Long userId, Long partyId);

    @Query("""
        select u
        from User u
        where u.id = (
            select pu.user.id from PartyUser pu where pu.party.id = :partyId and pu.isHost = true
        )
        """)
    Optional<User> findPartyHostByPartyId(@Param("partyId") Long partyId);

    @Modifying
    @Query("""
        update User u
        set u.point = u.point + :refundAmount, u.version = u.version + 1
        where u.id = (
            select pu.user.id from PartyUser pu where pu.party.id = :partyId and pu.isHost = true
        )
        """)
    void refundReservationFee(@Param("partyId") Long partyId,
        @Param("refundAmount") int refundAmount);
}
