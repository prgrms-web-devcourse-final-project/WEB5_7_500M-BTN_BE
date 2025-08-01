package shop.matjalalzz.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

public class TestUtil {

    public static User createUser() {
        String randomEmail = "testEmail" + UUID.randomUUID() + "@naver.com";
        return User.builder()
            .email(randomEmail)
            .role(Role.USER)
            .password("test-password")
            .nickname("nickname")
            .name("홍길동")
            .phoneNumber("010-1111-2222")
            .age(25)
            .gender(Gender.M)
            .oauthId(UUID.randomUUID().toString())
            .profileKey(UUID.randomUUID().toString())
            .build();
    }

    public static Shop createShop(User user) {
        return Shop.builder()
            .shopName("테스트 식당")
            .roadAddress("서울 강남대로 123")
            .detailAddress("서울특별시 강남구 101호")
            .roadAddress("서울특별시 강남구")
            .sido("서울")
            .latitude(37.5665)
            .longitude(126.9780)
            .description("테스트 설명입니다.")
            .category(FoodCategory.KOREAN)
            .tel("02-1234-5678")
            .businessCode("1234567890")
            .openTime(LocalTime.of(10, 0))
            .closeTime(LocalTime.of(22, 0))
            .user(user)
            .reservationFee(1000)
            .openTime(LocalTime.of(10, 0))
            .closeTime(LocalTime.of(22, 0))
            .build();
    }

    public static Party createParty(Shop shop) {
        return Party.builder()
            .title("맛집 원정대")
            .description("같이 먹어요!")
            .minCount(2)
            .maxCount(4)
            .minAge(20)
            .maxAge(35)
            .genderCondition(GenderCondition.A)
            .metAt(LocalDateTime.now().plusDays(1))
            .deadline(LocalDateTime.now().plusHours(3))
            .shop(shop)
            .build();
    }

    public static Reservation createReservation(Shop shop, User user, Party party,
        LocalDateTime reservedAt) {
        return Reservation.builder()
            .shop(shop)
            .user(user)
            .party(party)
            .headCount(2)
            .reservationFee(shop.getReservationFee())
            .reservedAt(reservedAt)
            .status(ReservationStatus.PENDING)
            .build();
    }

    public static Review createReview(String content, Double rating, Shop shop,
        Reservation reservation, User writer) {
        return Review.builder()
            .content(content)
            .rating(rating)
            .shop(shop)
            .reservation(reservation)
            .writer(writer)
            .build();
    }
}
