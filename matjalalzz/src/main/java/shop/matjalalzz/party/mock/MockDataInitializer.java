package shop.matjalalzz.party.mock;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.matjalalzz.party.mock.dao.MockShopRepository;
import shop.matjalalzz.party.mock.entity.MockShop2;

@Component
@RequiredArgsConstructor
public class MockDataInitializer {

    private final MockShopRepository mockShopRepository;

    @PostConstruct
    public void initShop() {
        mockShopRepository.save(new MockShop2(1L, "수유 곱창집", "서울특별시 지은구 지은로"));
    }

}
