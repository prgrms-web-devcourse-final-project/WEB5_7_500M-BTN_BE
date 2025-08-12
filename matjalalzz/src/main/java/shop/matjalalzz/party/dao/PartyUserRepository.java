package shop.matjalalzz.party.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.party.entity.PartyUser;

public interface PartyUserRepository extends JpaRepository<PartyUser, Long> {

    @Query(value = """
        select pu.* from party_user pu
        where pu.user_id = :userId and pu.party_id = :partyId
        """, nativeQuery = true)
    Optional<PartyUser> findByUserIdAndPartyId(@Param("userId") Long userId,
        @Param("partyId") Long partyId);

    List<PartyUser> findAllByPartyId(Long partyId);

    @Query(value = """
        select u.id as userId, u.nickname as userNickname, concat(:baseUrl, u.profileKey) as userProfile, pu.isHost
        from PartyUser pu
            join pu.user u
        where pu.party.id = :partyId
        """)
    <T> List<T> findMembersByPartyId(@Param("partyId") long partyId,
        @Param("baseUrl") String baseUrl);

    boolean existsByUserIdAndPartyId(Long userId, Long partyId);
}
