package shop.matjalalzz.shop.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShop is a Querydsl query type for Shop
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShop extends EntityPathBase<Shop> {

    private static final long serialVersionUID = -43361889L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShop shop = new QShop("shop");

    public final shop.matjalalzz.global.common.QBaseEntity _super = new shop.matjalalzz.global.common.QBaseEntity(this);

    public final EnumPath<Approve> approve = createEnum("approve", Approve.class);

    public final StringPath businessCode = createString("businessCode");

    public final EnumPath<FoodCategory> category = createEnum("category", FoodCategory.class);

    public final TimePath<java.time.LocalTime> closeTime = createTime("closeTime", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath description = createString("description");

    public final StringPath detailAddress = createString("detailAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final TimePath<java.time.LocalTime> openTime = createTime("openTime", java.time.LocalTime.class);

    public final NumberPath<Double> rating = createNumber("rating", Double.class);

    public final NumberPath<Integer> reservationFee = createNumber("reservationFee", Integer.class);

    public final StringPath roadAddress = createString("roadAddress");

    public final StringPath shopName = createString("shopName");

    public final StringPath sido = createString("sido");

    public final StringPath tel = createString("tel");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final shop.matjalalzz.user.entity.QUser user;

    public QShop(String variable) {
        this(Shop.class, forVariable(variable), INITS);
    }

    public QShop(Path<? extends Shop> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShop(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShop(PathMetadata metadata, PathInits inits) {
        this(Shop.class, metadata, inits);
    }

    public QShop(Class<? extends Shop> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new shop.matjalalzz.user.entity.QUser(forProperty("user")) : null;
    }

}

