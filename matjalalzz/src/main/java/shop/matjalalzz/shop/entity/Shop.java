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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.user.entity.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {@Index(name = "idx_shop_user", columnList = "user_id")}
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
    private double latitude; //위도

    @Column(nullable = false)
    private double longitude; //경도

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; //설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    @Column(length = 20, nullable = false)
    private String tel; //전화번호

    @Column(length = 10, nullable = false)
    private String businessCode; //사업자 번호

    @Column(nullable = false)
    private int reservationFee; //사장이 설정한 예약금

    @Column(nullable = false)
    private Double rating; // 별점

    private LocalTime openTime; //영업 시간 00:00

    private LocalTime closeTime; //영업 시간 00:00

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Approve approve;

    @Builder
    public Shop(String shopName, String roadAddress, String sido, Double latitude,
        Double longitude, String description, FoodCategory category, String tel,
        String businessCode, LocalTime openTime, LocalTime closeTime, User user,
        String detailAddress, int reservationFee) {
        this.shopName = shopName;
        this.roadAddress = roadAddress;
        this.sido = sido;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.category = category;
        this.tel = tel;
        this.businessCode = businessCode;
        this.reservationFee = reservationFee;
        this.rating = 0.0;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.user = user;
        this.detailAddress = detailAddress;
        this.approve = Approve.PENDING; // 기본으로 대기 상태
    }

    public void updateShop(ShopUpdateVo shopUpdateVo, User user) {
        this.shopName = shopUpdateVo.shopName();
        this.roadAddress = shopUpdateVo.roadAddress();
        this.sido = shopUpdateVo.sido();
        this.latitude = shopUpdateVo.latitude();
        this.longitude = shopUpdateVo.longitude();
        this.description = shopUpdateVo.description();
        this.category = shopUpdateVo.category();
        this.tel = shopUpdateVo.tel();
        this.businessCode = shopUpdateVo.businessCode();
        this.openTime = shopUpdateVo.openTime();
        this.closeTime = shopUpdateVo.closeTime();
        this.detailAddress = shopUpdateVo.detailAddress();
        this.user = user;
    }

    public void updateRating(double rating) {
        this.rating = rating;
    }

    public void updateApprove(Approve approve) {this.approve = approve;}

}
