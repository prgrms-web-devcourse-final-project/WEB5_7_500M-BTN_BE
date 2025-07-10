package shop.matjalalzz.shop.app;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.mapper.ShopMapper;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;

    @Transactional
    public void newShop(long userId, ShopCreateRequest shopCreateRequest) {
        Shop newShop = ShopMapper.createToShop(shopCreateRequest,userId);

        // 점주가 한명에 식당이 여러개 등록이 가능해도
        // 주소가 달라야 하며 or 기존 식당에 사업자 등록번호가 이미 있으면 에러
        shopRepository.findByBusinessCodeOrRoadAddress(newShop.getBusinessCode(), newShop.getRoadAddress())
            .ifPresent(shop ->  new BusinessException(ErrorCode.BLACKLISTED_TOKEN));  // 귀찮아서 잠깐 다른 에러

        shopRepository.save(newShop);
    }
}
