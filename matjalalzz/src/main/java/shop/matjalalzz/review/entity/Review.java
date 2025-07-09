package shop.matjalalzz.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.mock.MockImage;
import shop.matjalalzz.mock.MockReservation;
import shop.matjalalzz.mock.MockShop;
import shop.matjalalzz.mock.MockUser;
import shop.matjalalzz.global.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(
    indexes = {
        @Index(name = "idx_review_writer", columnList = "writer_id"),
        @Index(name = "idx_review_reservation", columnList = "reservation_id"),
        @Index(name = "idx_review_shop", columnList = "shop_id")
    }
)
public class Review extends BaseEntity {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 65_535, nullable = false)
    @Lob
    private String content;

    @Column(nullable = false)
    @Digits(integer = 1, fraction = 2)
    private Double rating = 0D;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private MockShop shop;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private MockReservation reservation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private MockUser writer;

    @OneToMany(mappedBy = "review")
    private List<MockImage> images;


}
