package shop.matjalalzz.review.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.QReview;
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

        if (!reviewProjections.isEmpty()) {
            List<Long> reviewIds = reviewProjections.stream()
                .map(ReviewProjection::getReviewId)
                .toList();

            List<Long> writerIds = reviewProjections.stream()
                .map(ReviewProjection::getWriterId)
                .distinct()
                .toList();

            // TODO: 사용자 정보 조회 분리
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

            // TODO: 이미지 정보 조회 분리
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

}
