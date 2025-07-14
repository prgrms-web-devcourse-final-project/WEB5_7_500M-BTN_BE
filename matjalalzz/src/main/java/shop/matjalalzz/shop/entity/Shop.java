package shop.matjalalzz.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.user.entity.User;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Str;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "idx_shop_user", columnList = "user_id") }
)
public class Shop extends BaseEntity {

    @Id
    @Column(name = "shop_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private String detailAddress;

    @Column(length = 10, nullable = false)
    private String sido;

    @Column(nullable = false)
    private Double latitude; //위도

    @Column(nullable = false)
    private Double longitude; //경도

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; //설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    @Column(length = 20, nullable = false)
    private String tel; //전화번호

    @Column(length = 10, nullable = false)
    private long businessCode; //사업자 번호

    @Column(nullable = false)
    private int reservationFee; //사장이 설정한 예약금

    @Column(nullable = false)
    private Double rating ; // 별점

    private LocalTime openTime; //영업 시간 00:00

    private LocalTime closeTime; //영업 시간 00:00

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    int imageCount;


    @Builder
    public Shop( String shopName, String roadAddress, String sido, Double latitude,
        Double longitude, String description, FoodCategory category, String tel,
        long businessCode, LocalTime openTime, LocalTime closeTime, User user, Integer imageCount, String detailAddress) {
        this.shopName = shopName;
        this.roadAddress = roadAddress;
        this.sido = sido;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.category = category;
        this.tel = tel;
        this.businessCode = businessCode;
        this.reservationFee = 0;
        this.rating = 0.0;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.user = user;
        this.imageCount = imageCount;
        this.detailAddress = detailAddress;
    }

    public void updateShop(ShopUpdateRequest dto) {
        this.shopName = dto.shopName();
        this.roadAddress = dto.roadAddress();
        this.sido = dto.sido();
        this.latitude = dto.latitude();
        this.longitude = dto.longitude();
        this.description = dto.description();
        this.category = dto.category();
        this.tel = dto.tel();
        this.businessCode = dto.businessCode();
        this.openTime = dto.openTime();
        this.closeTime = dto.closeTime();
        this.imageCount = dto.imageCount(); // 새 이미지 개수로 갱신
        this.detailAddress = dto.detailAddress();
    }

}
