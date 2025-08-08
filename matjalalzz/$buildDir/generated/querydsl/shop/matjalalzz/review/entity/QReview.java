package shop.matjalalzz.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = 44849955L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final shop.matjalalzz.global.common.QBaseEntity _super = new shop.matjalalzz.global.common.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<shop.matjalalzz.image.entity.Image, shop.matjalalzz.image.entity.QImage> images = this.<shop.matjalalzz.image.entity.Image, shop.matjalalzz.image.entity.QImage>createList("images", shop.matjalalzz.image.entity.Image.class, shop.matjalalzz.image.entity.QImage.class, PathInits.DIRECT2);

    public final NumberPath<Double> rating = createNumber("rating", Double.class);

    public final shop.matjalalzz.reservation.entity.QReservation reservation;

    public final shop.matjalalzz.shop.entity.QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final shop.matjalalzz.user.entity.QUser writer;

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reservation = inits.isInitialized("reservation") ? new shop.matjalalzz.reservation.entity.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
        this.shop = inits.isInitialized("shop") ? new shop.matjalalzz.shop.entity.QShop(forProperty("shop"), inits.get("shop")) : null;
        this.writer = inits.isInitialized("writer") ? new shop.matjalalzz.user.entity.QUser(forProperty("writer")) : null;
    }

}

