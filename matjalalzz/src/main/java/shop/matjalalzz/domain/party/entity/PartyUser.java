package shop.matjalalzz.domain.party.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import shop.matjalalzz.global.unit.BaseEntity;

@Entity
@Getter
@Table(name = "party_users")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartyUser extends BaseEntity {

    @Id
    @Column(name = "party_users_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    private boolean isHost;

    //추후 User 엔티티와 연관관계 필요

    public static PartyUser createHost(Party party) {
        return PartyUser.builder()
            .party(party)
            .isHost(true)
            .build();
    }

    public static PartyUser createUser(Party party) {
        return PartyUser.builder()
            .party(party)
            .isHost(false)
            .build();
    }
}
