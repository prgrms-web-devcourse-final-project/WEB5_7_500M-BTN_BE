package shop.matjalalzz.party;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;

@Profile("test")
@RequiredArgsConstructor
@Component
public class PartyInitializer {

    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @PostConstruct
    public void init() {
        User user = userRepository.save(User.builder()
            .email("ziening@ziening")
            .password("1234")
            .nickname("지닝")
            .name("지닝이")
            .gender(Gender.W)
            .phoneNumber("010-0000-0000")
            .build());

        shopRepository.save(Shop.builder()
            .id(1L)
            .name("지은맛집")
            .address("서울특별시 지은구 지은로")
            .sido(("서울"))
            .latitude(1.234)
            .longitude(1.234)
            .description("Gdgd")
            .category(FoodCategory.CHICKEN)
            .tel("02-020-000")
            .businessCode("000000")
            .reservationFee(0)
            .rating(0.0)
            .owner(user)
            .build());

        for (int i = 0; i < 33; i++) {
            partyRepository.save(Party.builder()
                .title("맛집탐험")
                .shop(shopRepository.findById(1L).get())
                .deadline(LocalDateTime.of(2025, 7, 12, 1, 1))
                .genderCondition(GenderCondition.A)
                .minAge(20)
                .maxAge(30)
                .metAt(LocalDateTime.of(2025, 7, 13, 1, 1))
                .minCount(3)
                .maxCount(7)
                .description("ㅎㅇㅎㅇ")
                .build());
        }
    }
}
