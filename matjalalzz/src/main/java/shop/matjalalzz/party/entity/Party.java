package shop.matjalalzz.party.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.party.mock.entity.MockShop2;

@Entity
@Getter
@Builder
@Table(name = "parties")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Party extends BaseEntity {

    @Id
    @Column(name = "party_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PartyStatus status = PartyStatus.RECRUITING;

    @Builder.Default
    private int currentCount = 1;

    private int minCount;

    private int maxCount;

    private int minAge;

    private int maxAge;

    @Enumerated(EnumType.STRING)
    private GenderCondition genderCondition;

    private LocalDateTime metAt;

    private LocalDateTime deadline;

    private int totalReservationFee;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private MockShop2 shop;

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyUser> partyUsers = new ArrayList<>();

}
