package shop.matjalalzz.review.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.review.entity.QReview;
import shop.matjalalzz.review.entity.Review;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Review> findByShopIdAndCursor(Long shopId, Long cursor, Pageable pageable) {
        QReview review = QReview.review;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.shop.id.eq(shopId));

        if (cursor != null) {
            builder.and(review.id.lt(cursor));
        }

        List<Review> reviews = queryFactory
            .selectFrom(review)
            .where(builder)
            .orderBy(review.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = reviews.size() > pageable.getPageSize();
        if (hasNext) {
            reviews.removeLast();
        }

        return new SliceImpl<>(reviews, pageable, hasNext);
    }
}
