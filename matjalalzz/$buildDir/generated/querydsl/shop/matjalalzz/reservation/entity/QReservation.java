package shop.matjalalzz.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = -1897518247L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final shop.matjalalzz.global.common.QBaseEntity _super = new shop.matjalalzz.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Integer> headCount = createNumber("headCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final shop.matjalalzz.party.entity.QParty party;

    public final NumberPath<Integer> reservationFee = createNumber("reservationFee", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> reservedAt = createDateTime("reservedAt", java.time.LocalDateTime.class);

    public final shop.matjalalzz.shop.entity.QShop shop;

    public final EnumPath<ReservationStatus> status = createEnum("status", ReservationStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final shop.matjalalzz.user.entity.QUser user;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.party = inits.isInitialized("party") ? new shop.matjalalzz.party.entity.QParty(forProperty("party"), inits.get("party")) : null;
        this.shop = inits.isInitialized("shop") ? new shop.matjalalzz.shop.entity.QShop(forProperty("shop"), inits.get("shop")) : null;
        this.user = inits.isInitialized("user") ? new shop.matjalalzz.user.entity.QUser(forProperty("user")) : null;
    }

}

