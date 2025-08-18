package shop.matjalalzz.party.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.MyPartyPageResponse;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 실행 순서 고정
public class MyPartyListPerfTest {

    @Autowired
    private PartyFacade partyFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopService shopService;
    @Autowired
    private PartyRepository partyRepository;
    @Autowired
    private PartyUserRepository partyUserRepository;
    @Autowired
    private UserService userService;

    @Test
    @Order(1)
    @Transactional
    @Commit
    void joinParty() {

        // given: 유저 100명 생성
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            User user = User.builder()
                .email("user" + i + "@ziening.com")
                .name("유저" + i)
                .nickname("닉네임" + i)
                .password("password")
                .role(Role.USER)
                .age(25)
                .gender(Gender.M)
                .phoneNumber("010-0000-" + String.format("%04d", i))
                .build();
            users.add(user);
        }
        userRepository.saveAll(users);

        Shop shop = shopService.shopFind(1L);

        // given: 파티 100개 생성
        List<Party> parties = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            Party party = Party.builder()
                .title("파티 " + i)
                .description("테스트 파티 " + i)
                .minCount(2)
                .maxCount(1500)
                .minAge(20)
                .maxAge(40)
                .genderCondition(GenderCondition.A)
                .metAt(LocalDateTime.now().plusDays(1))
                .deadline(LocalDateTime.now().plusDays(1).minusHours(1))
                .shop(shop)
                .build();
            parties.add(party);
        }
        partyRepository.saveAll(parties);

        // 100명의 유저가 1000개의 파티에 참여
        List<PartyUser> joins = new ArrayList<>(users.size() * parties.size());
        for (User user : users) {
            for (Party party : parties) {
                joins.add(PartyUser.createUser(party, user));
            }
        }
        partyUserRepository.saveAll(joins);
    }

    @Test
    @Order(2)
    @Transactional
    void findMyPartyList() {
        User user = userService.getUserByEmail("user50@ziening.com");
        MyPartyPageResponse response = partyFacade.findMyPartyPage(user.getId(), null, 10);
        for (MyPartyResponse content : response.content()) {
            System.out.print(content.partyId() + " ");
        }
    }

}
