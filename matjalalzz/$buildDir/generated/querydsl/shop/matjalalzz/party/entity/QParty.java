package shop.matjalalzz.party.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QParty is a Querydsl query type for Party
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QParty extends EntityPathBase<Party> {

    private static final long serialVersionUID = 1493461977L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QParty party = new QParty("party");

    public final shop.matjalalzz.global.common.QBaseEntity _super = new shop.matjalalzz.global.common.QBaseEntity(this);

    public final ListPath<shop.matjalalzz.comment.entity.Comment, shop.matjalalzz.comment.entity.QComment> comments = this.<shop.matjalalzz.comment.entity.Comment, shop.matjalalzz.comment.entity.QComment>createList("comments", shop.matjalalzz.comment.entity.Comment.class, shop.matjalalzz.comment.entity.QComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Integer> currentCount = createNumber("currentCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> deadline = createDateTime("deadline", java.time.LocalDateTime.class);

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final StringPath description = createString("description");

    public final EnumPath<shop.matjalalzz.party.entity.enums.GenderCondition> genderCondition = createEnum("genderCondition", shop.matjalalzz.party.entity.enums.GenderCondition.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxAge = createNumber("maxAge", Integer.class);

    public final NumberPath<Integer> maxCount = createNumber("maxCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> metAt = createDateTime("metAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> minAge = createNumber("minAge", Integer.class);

    public final NumberPath<Integer> minCount = createNumber("minCount", Integer.class);

    public final ListPath<PartyUser, QPartyUser> partyUsers = this.<PartyUser, QPartyUser>createList("partyUsers", PartyUser.class, QPartyUser.class, PathInits.DIRECT2);

    public final shop.matjalalzz.reservation.entity.QReservation reservation;

    public final shop.matjalalzz.shop.entity.QShop shop;

    public final EnumPath<shop.matjalalzz.party.entity.enums.PartyStatus> status = createEnum("status", shop.matjalalzz.party.entity.enums.PartyStatus.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> totalReservationFee = createNumber("totalReservationFee", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QParty(String variable) {
        this(Party.class, forVariable(variable), INITS);
    }

    public QParty(Path<? extends Party> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QParty(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QParty(PathMetadata metadata, PathInits inits) {
        this(Party.class, metadata, inits);
    }

    public QParty(Class<? extends Party> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reservation = inits.isInitialized("reservation") ? new shop.matjalalzz.reservation.entity.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
        this.shop = inits.isInitialized("shop") ? new shop.matjalalzz.shop.entity.QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

