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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.user.entity.User;

@Entity
@Getter
@Table(name = "party_users")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
public class PartyUser extends BaseEntity {

    @Id
    @Column(name = "party_users_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isHost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
}
