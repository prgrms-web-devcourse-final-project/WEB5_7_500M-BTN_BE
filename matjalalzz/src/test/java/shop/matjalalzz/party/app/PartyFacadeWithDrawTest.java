package shop.matjalalzz.party.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.app.query.ShopQueryService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

@SpringBootTest
@Transactional
public class PartyFacadeWithDrawTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopQueryService shopQueryService;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PartyFacade partyFacade;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("회원 탈퇴 시 본인이 주인장인 파티들도 삭제됨")
    void deletePartyForWithdraw() {
        User user = User.builder()
            .email("user1@ziening.com")
            .name("유저")
            .nickname("닉네임")
            .password("password")
            .role(Role.USER)
            .age(25)
            .gender(Gender.M)
            .phoneNumber("010-0000-0000")
            .build();

        userRepository.save(user);

        Shop shop = shopQueryService.findShop(1L);

        Party party1 = Party.builder()
            .title("파티1")
            .description("테스트 파티1")
            .minCount(2)
            .maxCount(1500)
            .minAge(20)
            .maxAge(40)
            .genderCondition(GenderCondition.A)
            .metAt(LocalDateTime.now().plusDays(1))
            .deadline(LocalDateTime.now().plusDays(1).minusHours(1))
            .shop(shop)
            .build();

        Party party2 = Party.builder()
            .title("파티2")
            .description("테스트 파티12")
            .minCount(2)
            .maxCount(1500)
            .minAge(20)
            .maxAge(40)
            .genderCondition(GenderCondition.A)
            .metAt(LocalDateTime.now().plusDays(1))
            .deadline(LocalDateTime.now().plusDays(1).minusHours(1))
            .shop(shop)
            .build();

        partyRepository.save(party1);
        partyRepository.save(party2);

        Reservation reservation = Reservation.builder()
            .headCount(1)
            .reservationFee(1000)
            .reservedAt(LocalDateTime.now())
            .status(ReservationStatus.CONFIRMED)
            .user(user)
            .shop(shop)
            .party(party2)
            .build();

        reservationRepository.save(reservation);

        PartyUser host1 = PartyUser.createHost(party1, user);
        PartyUser host2 = PartyUser.createHost(party2, user);

        party1.getPartyUsers().add(host1);
        party2.getPartyUsers().add(host2);

        partyFacade.deletePartyForWithdraw(user);

        partyRepository.flush();
        em.clear();

        // party1은 삭제되어야함
        Optional<Party> found1 = partyRepository.findById(party1.getId());
        assertThat(found1).isEmpty();

        // party2는 삭제 불가능(예약일이 하루전임)
        Optional<Party> found2 = partyRepository.findById(party2.getId());
        assertThat(found2).isPresent();
    }

}
