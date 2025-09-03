package shop.matjalalzz.shop.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ApproveRequest;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ShopCommendService {

    private final ShopRepository shopRepository;

    // 식당 상태 변경
    public Shop approveUpdate(long shopId, ApproveRequest approveRequest) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));

        if (shop.getApprove().equals(approveRequest.approve())) {
            return null;
        }
        shop.updateApprove(approveRequest.approve());
        return shop;
    }

    public void createNewShop(Shop shop) {
        shopRepository.save(shop);
    }

    // shop 수정
    public void editShop(Shop shop, ShopUpdateVo shopUpdateVo, User user) {
        shop.updateShop(shopUpdateVo, user);
    }







}
