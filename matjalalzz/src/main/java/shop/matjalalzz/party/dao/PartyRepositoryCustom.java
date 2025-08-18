package shop.matjalalzz.party.dao;

import java.util.List;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;

public interface PartyRepositoryCustom {

    List<Party> searchWithCursor(PartySearchParam cond, int size);
}
