package shop.matjalalzz.user.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Long, String> getUsersNickname(List<Long> ids) {
        QUser user = QUser.user;
        Map<Long, String> userMap = queryFactory
            .select(user.id, user.nickname)
            .from(user)
            .where(user.id.in(ids))
            .stream()
            .collect(Collectors.toMap(
                    t -> t.get(user.id),
                    t -> t.get(user.nickname)
                )
            );
        return userMap;
    }
}
