package shop.matjalalzz.party.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.user.entity.User;

public interface PartyUserRepository extends JpaRepository<PartyUser, Long> {

    Optional<PartyUser> findByUserIdAndPartyId(Long userId, Long partyId);

    List<PartyUser> findAllByPartyId(Long partyId);

    @Query("""
        select u
        from User u
        where u.id = (
            select pu.user.id from PartyUser pu where pu.party.id = :partyId and pu.isHost = true
        )
        """)
    Optional<User> findPartyHostByPartyId(@Param("partyId") Long partyId);

    @Query("""
        update User u
        set u.point = u.point + :refundAmount
        where u.id = (
            select pu.user.id from PartyUser pu where pu.party.id = :partyId and pu.isHost = true
        )
        """)
    void refundReservationFee(@Param("partyId") Long partyId, @Param("refundAmount") int refundAmount);
}
