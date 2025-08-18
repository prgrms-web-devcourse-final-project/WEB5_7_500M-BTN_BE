package shop.matjalalzz.party.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.QParty;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.QShop;

@RequiredArgsConstructor
public class PartyRepositoryImpl {

    private final JPAQueryFactory query;

    //Q-types
    private static final QParty party = QParty.party;
    private static final QShop shop = QShop.shop;

    public List<Party> searchWithCursor(PartySearchParam cond, int size) {
        return baseQuery(cond)
            .orderBy(party.id.desc())
            .limit(size + 1)
            .fetch();
    }

    private JPAQuery<Party> baseQuery(PartySearchParam cond) {
        return query.selectFrom(party)
            .join(party.shop, shop).fetchJoin()
            .where(
                statusEq(cond.status()),
                genderEq(cond.gender()),
                ageMinGoe(cond.minAge()),
                ageMaxLoe(cond.maxAge()),
                sidoEq(cond.location()),
                categoriesIn(cond.categories()),
                titleContains(cond.query()),
                cursorLt(cond.cursor())
            );
    }

    // --- 동적 조건들 ---
    private BooleanExpression statusEq(PartyStatus status) {
        return status == null ? null : party.status.eq(status);
    }

    private BooleanExpression genderEq(GenderCondition gender) {
        return gender == null ? null : party.genderCondition.eq(gender);
    }

    private BooleanExpression ageMinGoe(Integer minAge) {
        return minAge == null ? null : party.minAge.goe(minAge);
    }

    private BooleanExpression ageMaxLoe(Integer maxAge) {
        return maxAge == null ? null : party.maxAge.loe(maxAge);
    }

    private BooleanExpression sidoEq(String sido) {
        return (sido == null || sido.isBlank()) ? null : shop.sido.eq(sido);
    }

    private BooleanExpression categoriesIn(List<FoodCategory> categories) {
        return (categories == null || categories.isEmpty()) ? null : shop.category.in(categories);
    }

    private BooleanExpression titleContains(String query) {
        return (query == null || query.isBlank()) ? null : party.title.containsIgnoreCase(query);
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : party.id.lt(cursor);
    }
}
