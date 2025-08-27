package shop.matjalalzz.party.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepositoryCustom {

    Slice<Party> searchWithCursor(PartySearchParam cond, Pageable pageable);
}
