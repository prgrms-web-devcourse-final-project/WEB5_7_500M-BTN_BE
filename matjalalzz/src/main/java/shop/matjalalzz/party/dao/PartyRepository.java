package shop.matjalalzz.party.dao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepository extends JpaRepository<Party, Long>,
    JpaSpecificationExecutor<Party> {

    @Query("""
        select new shop.matjalalzz.party.dto.MyPartyResponse(
                p.id, p.title, s.shopName, p.metAt, p.deadline, p.status, p.maxCount, p.minCount,
                p.currentCount, p.genderCondition, p.minAge, p.maxAge, p.description
        )
        from Party p
            join p.shop  s
        where (:cursor is null or p.id < :cursor)
            and exists (
                select 1 from PartyUser pu
                where pu.party = p
                    and pu.user.id = :userId
            )
        order by p.id desc
        """)
    Slice<MyPartyResponse> findByUserIdAndCursor(Long userId, Long cursor, PageRequest of);

}
