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
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.image.entity.QImage;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.entity.QReview;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.QShop;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Review> findByShopIdAndCursor(Long shopId, Long cursor, Pageable pageable) {
        QReview review = QReview.review;
        QImage image = QImage.image;

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
        if (!reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream()
                .map(Review::getId)
                .toList();

            Map<Long, List<Image>> imageMap = queryFactory
                .selectFrom(image)
                .where(image.reviewId.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(Image::getReviewId));

            // Shop에 Image 매핑
            reviews.forEach(r ->
                r.setImages(imageMap.getOrDefault(r.getId(), List.of())));
        }

        return new SliceImpl<>(reviews, pageable, hasNext);
    }

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
