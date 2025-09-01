package shop.matjalalzz.review.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.image.entity.QImage;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.QReview;
import shop.matjalalzz.shop.entity.QShop;
import shop.matjalalzz.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ReviewProjection> findByShopIdAndCursor(Long shopId, Long cursor,
        Pageable pageable) {
        QReview review = QReview.review;
        QImage image = QImage.image;
        QUser user = QUser.user;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.shop.id.eq(shopId));
        builder.and(cursorDescCondition(cursor));

        // 1. 먼저 리뷰 정보를 중간 DTO로 조회
        List<ReviewProjection> reviewProjections = queryFactory
            .select(Projections.fields(ReviewProjection.class,
                review.id.as("reviewId"),
                review.writer.id.as("writerId"),
                review.rating,
                review.content,
                review.createdAt
            ))
            .from(review)
            .where(builder)
            .orderBy(review.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = processPage(pageable, reviewProjections);

        return new SliceImpl<>(reviewProjections, pageable, hasNext);
    }

    private boolean processPage(Pageable pageable, List<ReviewProjection> reviewProjections) {
        boolean hasNext = reviewProjections.size() > pageable.getPageSize();
        if (hasNext) {
            reviewProjections.removeLast();
        }
        return hasNext;
    }

    private BooleanExpression cursorDescCondition(Long cursor) {
        if (cursor != null) {
            return QReview.review.id.lt(cursor);
        } else {
            return null;
        }
    }

    //TODO: 병합 이후 제거
    @Override
    public Slice<MyReviewResponse> findByUserIdAndCursor(Long userId, Long cursor,
        Pageable pageable) {
        QReview review = QReview.review;
        QShop shop = QShop.shop;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.writer.id.eq(userId));
        builder.and(cursorDescCondition(cursor));

        List<MyReviewResponse> reviews = queryFactory
            .select(Projections.constructor(MyReviewResponse.class,
                review.id,
                shop.shopName,
                review.rating,
                review.content,
                review.createdAt,
                null
            ))
            .from(review)
            .join(review.shop, shop)
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
