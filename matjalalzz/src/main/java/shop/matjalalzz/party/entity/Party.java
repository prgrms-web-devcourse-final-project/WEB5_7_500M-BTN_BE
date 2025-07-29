package shop.matjalalzz.party.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.shop.entity.Shop;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false")
public class Party extends BaseEntity {

    @Id
    @Column(name = "party_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private PartyStatus status;

    private int currentCount;

    private int minCount;

    private int maxCount;

    private int minAge;

    private int maxAge;

    @Enumerated(EnumType.STRING)
    private GenderCondition genderCondition;

    private LocalDateTime metAt;

    private LocalDateTime deadline;

    private int totalReservationFee;

    @Version
    private int version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reservation reservation;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyUser> partyUsers;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Builder
    public Party(String title, String description, int minCount, int maxCount, int minAge,
        int maxAge,
        GenderCondition genderCondition, LocalDateTime metAt, LocalDateTime deadline, Shop shop) {
        this.title = title;
        this.description = description;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.genderCondition = genderCondition;
        this.metAt = metAt;
        this.deadline = deadline;
        this.shop = shop;
        status = PartyStatus.RECRUITING;
        currentCount = 1;
        partyUsers = new ArrayList<>();
    }

    public void complete() {
        status = PartyStatus.COMPLETED;
    }

    public void terminate() {
        status = PartyStatus.TERMINATED;
    }

    // 연관된 PartyUser와 comments까지 cascade soft delete하는 메서드
    public void deleteParty() {
        super.delete();
        this.partyUsers.forEach(BaseEntity::delete);
        this.comments.forEach(BaseEntity::delete);
    }

    public void increaseCurrentCount() {
        this.currentCount += 1;
    }

    public void decreaseCurrentCount() {
        this.currentCount -= 1;
    }

    public boolean isRecruiting() {
        return this.status == PartyStatus.RECRUITING;
    }

    public void increaseTotalReservationFee(int reservationFee) {
        this.totalReservationFee += reservationFee;
    }

    public void decreaseTotalReservationFee(int reservationFee) {
        this.totalReservationFee -= reservationFee;
    }
}
