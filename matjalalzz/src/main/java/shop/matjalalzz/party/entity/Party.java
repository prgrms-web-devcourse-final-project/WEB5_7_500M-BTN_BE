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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import shop.matjalalzz.party.mock.entity.MockShop;

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

    @NotNull
    @Size(max = 50)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @NotNull
    @Size(max = 15)
    private PartyStatus status = PartyStatus.RECRUITING;

    @Builder.Default
    @NotNull
    private int currentCount = 1;

    @NotNull
    private int minCount;

    @NotNull
    private int maxCount;

    @NotNull
    private int minAge;

    @NotNull
    private int maxAge;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Size(max = 1)
    private GenderCondition genderCondition;

    @NotNull
    private LocalDateTime metAt;

    @NotNull
    private LocalDateTime deadline;

    @NotNull
    private int totalReservationFee;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    @NotNull
    private MockShop shop;

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyUser> partyUsers = new ArrayList<>();

}
