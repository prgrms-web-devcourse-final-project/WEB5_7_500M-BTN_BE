package shop.matjalalzz.domain.party.mock;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.matjalalzz.domain.party.mock.dao.MockShopRepository;
import shop.matjalalzz.domain.party.mock.dao.MockUserRepository;
import shop.matjalalzz.domain.party.mock.entity.MockShop;
import shop.matjalalzz.domain.party.mock.entity.MockUser;

@Component
@RequiredArgsConstructor
public class MockDataInitializer {

    private final MockShopRepository mockShopRepository;
    private final MockUserRepository mockUserRepository;

    @PostConstruct
    public void initShop() {
        mockShopRepository.save(new MockShop(1L, "수유 곱창집", "서울특별시 지은구 지은로"));
    }

    @PostConstruct
    public void initUser() {
        mockUserRepository.save(new MockUser(1L));
        mockUserRepository.save(new MockUser(2L));
    }

}
