package shop.matjalalzz.review.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.image.entity.QImage;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewProjection;
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

        if (cursor != null) {
            builder.and(review.id.lt(cursor));
        }

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

        boolean hasNext = reviewProjections.size() > pageable.getPageSize();
        if (hasNext) {
            reviewProjections.removeLast();
        }

        if (!reviewProjections.isEmpty()) {
            List<Long> reviewIds = reviewProjections.stream()
                .map(ReviewProjection::getReviewId)
                .toList();

            List<Long> writerIds = reviewProjections.stream()
                .map(ReviewProjection::getWriterId)
                .distinct()
                .toList();

            // 2. 사용자 정보 조회
            Map<Long, String> userMap = queryFactory
                .select(user.id, user.nickname)
                .from(user)
                .where(user.id.in(writerIds))
                .stream()
                .collect(Collectors.toMap(
                        t -> t.get(user.id),
                        t -> t.get(user.nickname)
                    )
                );

            // 3. 이미지 정보 조회
            Map<Long, List<String>> imageMap = queryFactory
                .select(image.reviewId, image.s3Key)
                .from(image)
                .where(image.reviewId.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                    t -> t.get(image.reviewId),
                    Collectors.mapping(t -> t.get(image.s3Key), Collectors.toList())
                ));

            // 4. 최종 ReviewResponse 생성
            reviewProjections.stream()
                .forEach(r -> {
                    r.setUserNickname(userMap.get(r.getWriterId()));
                    r.setImages(imageMap.get(r.getReviewId()));
                });
        }

        return new SliceImpl<>(reviewProjections, pageable, hasNext);
    }

    //TODO: 병합 이후 제거
    @Override
    public Slice<MyReviewResponse> findByUserIdAndCursor(Long userId, Long cursor,
        Pageable pageable) {
        QReview review = QReview.review;
        QShop shop = QShop.shop;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(review.writer.id.eq(userId));

        if (cursor != null && cursor != 0) {
            builder.and(review.id.lt(cursor));
        }

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
