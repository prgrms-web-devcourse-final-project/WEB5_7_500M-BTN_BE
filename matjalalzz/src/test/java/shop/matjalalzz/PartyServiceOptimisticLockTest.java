package shop.matjalalzz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.app.PartyChatService;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

@Slf4j
@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // 각 쓰레드가 독립 트랜잭션
class PartyServiceOptimisticLockTest {

    @Autowired PartyService partyService;
    @Autowired PartyRepository partyRepository;
    @Autowired UserService userService;
    @Autowired
    ShopRepository shopRepository;   // 테스트용
    @Autowired
    UserRepository userRepository;
    @MockBean
    PartyChatService partyChatService;

    @Test
    @DisplayName("임시 낙관적 락 테스트")
    void optimisticLockRetryableTest() throws Exception {
        /* ── GIVEN ──────────────────────────────────────── */
        // 1) 파티 호스트 + Shop 준비
        User host = userRepository.save(mockUser("host@example.com", Role.OWNER));
        Shop shop = shopRepository.save(mockShop("테스트맛집", host));
        int maxCount = 10;
        int threadCount = 20;

        Party party = Party.builder()
            .title("동시성 테스트 파티")
            .description("optimistic‑lock test")
            .minCount(1)
            .maxCount(maxCount)
            .minAge(0).maxAge(100)
            .genderCondition(GenderCondition.A)
            .metAt(LocalDateTime.now().plusDays(1))
            .deadline(LocalDateTime.now().plusHours(3))
            .shop(shop)
            .build();
        partyRepository.save(party);                    // host 1명, version = 0

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threadCount);
        ExecutorService es   = Executors.newFixedThreadPool(threadCount);

        /* ── WHEN : 동시에 joinParty 호출 ────────────────── */
        for (int i = 0; i < threadCount; i++) {
            User u = userRepository.save(mockUser("user"+i+"@ex.com", Role.USER));
            es.submit(() -> {
                ready.countDown();      // 준비 완료
                try {
                    start.await();      // 동시에 시작
                    partyService.joinParty(party.getId(), u.getId());  // @Retryable + 낙관적락
                } catch (Exception e) {
                    log.error("exception: {}", e.getMessage());
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();   // 모든 쓰레드 준비될 때까지
        start.countDown();
        done.await();
        es.shutdown();

        /* ── THEN : 버전·인원 검증 ───────────────────────── */
        Party reloaded = partyRepository.findById(party.getId()).orElseThrow();
        assertEquals(maxCount, reloaded.getCurrentCount());
        assertEquals(maxCount - 1, reloaded.getVersion(), "version 값이 참가자 수만큼 증가해야 한다");
    }

    /* --------- 헬퍼 메서드 ---------- */

    private User mockUser(String email, Role role) {
        return User.builder()
            .email(email)
            .nickname(email)
            .phoneNumber("010‑0000‑0000")
            .name("홍길동")
            .age(5)
            .gender(Gender.M)
            .role(role)
            .build();
    }

    private Shop mockShop(String name, User owner) {
        return Shop.builder()
            .shopName(name)
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
            .user(owner)
            .reservationFee(1000)
            .openTime(LocalTime.of(10, 0))
            .closeTime(LocalTime.of(22, 0))
            .build();
    }
}