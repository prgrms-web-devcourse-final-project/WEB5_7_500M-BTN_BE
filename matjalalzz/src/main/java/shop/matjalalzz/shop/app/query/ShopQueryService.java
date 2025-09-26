package shop.matjalalzz.shop.app.query;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.AdminFindShopInfo;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.dto.projection.OwnerShopProjection;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopListSort;

@Service
@RequiredArgsConstructor
public class ShopQueryService {

    private final ShopRepository shopRepository;


    // 관리자가 등록을 원하는 상점들 리스트를 전부 가져옴
    @Transactional(readOnly = true)
    public List<Shop> adminFindAllPendingShop() {
        return shopRepository.findByApprove(Approve.PENDING);
    }

    // 관리자가 상점에 대한 상세 정보를 보는 용도 (상점에 상태와 관계 없이 다 가져옴)
    @Transactional(readOnly = true)
    public AdminFindShopInfo adminGetShop(long shopId) {
        return shopRepository.adminFindShop(shopId);
    }

    @Transactional(readOnly = true)
    public Optional<Shop> newShopCheck(String businessCode, String roadAddress,
        String detailAddress) {
        return shopRepository.findByBusinessCodeOrRoadAddressAndDetailAddress(
            businessCode, roadAddress, detailAddress);
    }


    @Transactional(readOnly = true)
    public Shop findShop(Long shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }

    @Transactional(readOnly = true)
    public List<Shop> findByOwnerId(Long ownerId) {
        return shopRepository.findByUserId(ownerId);
    }

    // 상태가 등록 된 식당만 가져와서 보여줌
    @Transactional(readOnly = true)
    public Optional<Shop> findOneShop(Long shopId) {
        return shopRepository.findByIdAndApprove(shopId, Approve.APPROVED);
    }

    // 사장이 가진 식당들 전부 조히
    @Transactional(readOnly = true)
    public List<OwnerShopProjection> findOwnerShopList(Long userId) {
        return shopRepository.findOwnerShopsWithFirstImage(userId);
    }


    @Transactional(readOnly = true)
    // 사장이 자신의 shop 하나를 상세 조회
    public Shop findOwnerShop(Long shopId, Long userId) {
        return shopRepository.findByIdAndUserId(shopId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }

    @Transactional(readOnly = true)
    // 거리순이나 별점순으로 상점 조회
    public List<ShopsItem> findDistanceOrRatingShops(double latitude, double longitude, double radius,
        List<FoodCategory> foodCategories, Double distanceOrRating, int size, String sort, Long shopId) {
        return shopRepository.findDistanceOrRatingShopsQdsl(latitude, longitude, radius, foodCategories, distanceOrRating, size, sort, shopId);
    }


    @Transactional(readOnly = true)
    public Slice<Shop> findShopCursorList (Object cursor, String query, Approve approve, Pageable pageable, ShopListSort sort){
        return shopRepository.findShopCursorList(cursor, query,  approve, pageable, sort);
    }

    public void validShop(Long shopId, Long ownerId) {
        if (shopId != null) {
            Shop shop = findShop(shopId);
            if (!shop.getUser().getId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        } else {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
    }





}
