package shop.matjalalzz.party.dao;

import static org.springframework.util.StringUtils.hasText;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;

public class PartySpecification {

    public static Specification<Party> hasStatus(PartyStatus status) {
        return (root, query, cb) ->
            status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Party> hasGender(GenderCondition gender) {
        return (root, query, cb) ->
            gender != null ? cb.equal(root.get("genderCondition"), gender) : null;
    }

    // 사용자의 age가 파티의 나이 제한 범위 안에 포함되는지 확인하는 조건 (로그인 시에만 가능)
//    public static Specification<Party> matchesAge(Boolean ageFilter, Integer userAge) {
//        return (root, query, cb) ->
//            ageFilter ? cb.and(
//                cb.lessThanOrEqualTo(root.get("minAge"), userAge),
//                cb.greaterThanOrEqualTo(root.get("maxAge"), userAge)
//            ) : null;
//    }

    //파라미터로 넘어온 age 범위 안에 파티의 age 범위가 포함되는지 확인
    public static Specification<Party> matchesAgeRange(Integer minAge, Integer maxAge) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (minAge != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("minAge"), minAge));
            }
            if (maxAge != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("maxAge"), maxAge));
            }

            return predicate.getExpressions().isEmpty() ? null : predicate;
        };
    }

    public static Specification<Party> containsSido(String sido) {
        return (root, query, cb) ->
            hasText(sido) ? cb.equal(root.get("shop").get("sido"), sido) : null;
    }

    public static Specification<Party> containsCategories(List<FoodCategory> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }

            return root.get("shop").get("category").in(categories);
        };
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

    public static Specification<Party> createSpecification(PartySearchParam condition) {
        return Specification.where(hasStatus(condition.status()))
            .and(hasGender(condition.gender()))
//            .and(matchesAge(condition.ageFilter(), userAge))
            .and(matchesAgeRange(condition.minAge(), condition.maxAge()))
            .and(containsSido(condition.location()))
            .and(containsCategories(condition.categories()))
            .and(containsQuery(condition.query()))
            .and(lessThanCursor(condition.cursor()));
    }


}
