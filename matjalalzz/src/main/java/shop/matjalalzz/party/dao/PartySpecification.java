package shop.matjalalzz.party.dao;

import static org.springframework.util.StringUtils.hasText;

import org.springframework.data.jpa.domain.Specification;
import shop.matjalalzz.party.dto.PartySearchCondition;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;

public class PartySpecification {

    public static Specification<Party> hasStatus(PartyStatus status) {
        //status는 일단 null일 수 없음 (recruiting이라는 기본값 존재)
        return (root, query, cb) ->
            status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Party> hasGender(GenderCondition gender) {
        return (root, query, cb) ->
            gender != null ? cb.equal(root.get("genderCondition"), gender) : null;
    }

    public static Specification<Party> matchesAge(Boolean ageFilter, Integer userAge) {
        return (root, query, cb) ->
            ageFilter ? cb.and(
                cb.lessThanOrEqualTo(root.get("minAge"), userAge),
                cb.greaterThanOrEqualTo(root.get("maxAge"), userAge)
            ) : null;
    }

    public static Specification<Party> containsSido(String sido) {
        return (root, query, cb) ->
            hasText(sido) ? cb.equal(root.get("shop").get("sido"), sido) : null;
    }

    public static Specification<Party> containsCategory(FoodCategory category) {
        return (root, query, cb) ->
            category != null ? cb.equal(root.get("shop").get("category"), category) : null;
    }

    public static Specification<Party> containsQuery(String queryStr) {
        return (root, query, cb) ->
            hasText(queryStr) ? cb.like(cb.lower(root.get("title")),
                "%" + queryStr.toLowerCase() + "%") : null;
    }

    public static Specification<Party> lessThanCursor(Long cursor) {
        return (root, query, cb) ->
            cursor != null ? cb.lessThan(root.get("id"), cursor) : null;
    }

    public static Specification<Party> createSpecification(PartySearchCondition condition,
        int userAge) {
        return Specification.where(hasStatus(condition.status()))
            .and(hasGender(condition.gender()))
            .and(matchesAge(condition.ageFilter(), userAge))
            .and(containsSido(condition.location()))
            .and(containsCategory(condition.category()))
            .and(containsQuery(condition.query()))
            .and(lessThanCursor(condition.cursor()));
    }


}
