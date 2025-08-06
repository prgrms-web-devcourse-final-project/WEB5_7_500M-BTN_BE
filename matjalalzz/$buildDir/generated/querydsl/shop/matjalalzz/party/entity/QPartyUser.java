package shop.matjalalzz.party.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPartyUser is a Querydsl query type for PartyUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPartyUser extends EntityPathBase<PartyUser> {

    private static final long serialVersionUID = 653342532L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPartyUser partyUser = new QPartyUser("partyUser");

    public final shop.matjalalzz.global.common.QBaseEntity _super = new shop.matjalalzz.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final BooleanPath deleted = _super.deleted;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isHost = createBoolean("isHost");

    public final QParty party;

    public final BooleanPath paymentCompleted = createBoolean("paymentCompleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final shop.matjalalzz.user.entity.QUser user;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QPartyUser(String variable) {
        this(PartyUser.class, forVariable(variable), INITS);
    }

    public QPartyUser(Path<? extends PartyUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPartyUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPartyUser(PathMetadata metadata, PathInits inits) {
        this(PartyUser.class, metadata, inits);
    }

    public QPartyUser(Class<? extends PartyUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.party = inits.isInitialized("party") ? new QParty(forProperty("party"), inits.get("party")) : null;
        this.user = inits.isInitialized("user") ? new shop.matjalalzz.user.entity.QUser(forProperty("user")) : null;
    }

}

