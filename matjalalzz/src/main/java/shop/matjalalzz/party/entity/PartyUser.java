package shop.matjalalzz.party.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.user.entity.User;

@Entity
@Getter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_party", columnNames = {"user_id", "party_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
public class PartyUser extends BaseEntity {

    @Id
    @Column(name = "party_users_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isHost;

    private boolean paymentCompleted;

    @Version
    private int version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public PartyUser(boolean isHost, Party party, User user) {
        this.isHost = isHost;
        this.party = party;
        this.user = user;
        this.paymentCompleted = false;
    }

    public static PartyUser createHost(Party party, User user) {
        return PartyUser.builder()
            .party(party)
            .user(user)
            .isHost(true)
            .build();
    }

    public static PartyUser createUser(Party party, User user) {
        return PartyUser.builder()
            .party(party)
            .user(user)
            .isHost(false)
            .build();
    }

    public void completePayment() {
        this.paymentCompleted = true;
    }
}
