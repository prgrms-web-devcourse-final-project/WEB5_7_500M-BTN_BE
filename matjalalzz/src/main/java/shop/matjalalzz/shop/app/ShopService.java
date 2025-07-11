package shop.matjalalzz.shop.app;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopOwnerResponse;
import shop.matjalalzz.shop.dto.ShopResponse;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional
    public void newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Shop newShop = ShopMapper.createToShop(shopCreateRequest,user);

        // 점주가 한명에 식당이 여러개 등록이 가능해도
        // 주소가 달라야 하며 or 기존 식당에 사업자 등록번호가 이미 있으면 에러
        shopRepository.findByBusinessCodeOrRoadAddress(newShop.getBusinessCode(), newShop.getRoadAddress())
            .ifPresent(shop ->  {throw new BusinessException(ErrorCode.DUPLICATE_SHOP); });

        shopRepository.save(newShop);
    }

    public ShopResponse getShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));



    }

//    public ShopOwnerResponse getOwnerShop(Long shopId, long userId) {
//
//
//        return
//    }
}
