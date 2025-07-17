package shop.matjalalzz.shop.app;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;

import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final PreSignedProvider preSignedProvider;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public PreSignedUrlListResponse newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userService.getUserById(userId);

        Shop newShop = ShopMapper.createToShop(shopCreateRequest, user);

        // 점주가 한명이며 한명이 식당 여러개 등록이 가능해도 식당 주소는 다 달라야 하며 or 사업자 등록번호가 이미 있으면 에러
        shopRepository.findByBusinessCodeOrRoadAddressAndDetailAddress(newShop.getBusinessCode(),
                newShop.getRoadAddress(), newShop.getDetailAddress())
            .ifPresent(shop -> {
                throw new BusinessException(ErrorCode.DUPLICATE_SHOP);
            });

        shopRepository.save(newShop);

        // 프리사이드 url 링크 반환
        return preSignedProvider.createShopUploadUrls(shopCreateRequest.imageCount(),
            newShop.getId());

    }

    @Transactional(readOnly = true)
    public ShopDetailResponse getShop(Long shopId) {

        // 해당 상점이 없으면 에러
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));

        //유저의 경우 로그인 한 사람, 안한 사람, 사장 3명이 존재하니 상점 주인인 경우 boolean값을 true로 반환하여 수정 버튼이 생기도록 반환
        boolean canEdit = false;

        List<String> imageUrllList = Optional.ofNullable(
                imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewRepository.findReviewCount(shop.getId());

        return ShopMapper.shopDetailResponse(shop, imageUrllList, canEdit, reviewCount);

    }


    @Transactional(readOnly = true)
    // 사장이 자신의 shop을 조회 한 경우 수정 허용되게
    public ShopOwnerDetailResponse getShopOwner(Long shopId, Long userId) {

        // 해당 상점이 없으면 에러
        Shop shop = shopFind(shopId);

        //유저의 경우 로그인 한 사람, 안한 사람, 사장 3명이 존재하니 상점 주인인 경우 boolean값을 true로 반환하여 수정 버튼이 생기도록 반환
        boolean canEdit = shop.getUser().getId().equals(userId);

        //사진 리스트로 가져왔을 때 없어도 에러는 반환 X    이거 mapper로 이동해야 함
        List<String> imageUrlList = Optional.ofNullable(
                imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewRepository.findReviewCount(shop.getId());

        return ShopMapper.shopOwnerDetailResponse(shop, imageUrlList, canEdit, reviewCount);

    }


    // shop 수정
    @Transactional
    public PreSignedUrlListResponse editShop(Long shopId, long userId,
        ShopUpdateRequest updateRequest) {

        // 해당 유저 정보를 가져오고
        User user = userService.getUserById(userId);

        // 해당 유저가 가진 shop들 리스트를 가져오고
        Shop shop = shopFind(shopId);

        // 수정을 원하는 shop을 가진 상점 주인이 맞는지 판단 후
        if (!shop.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_SHOP_OWNER);
        }

        // 해당 상점을 가져온다
        List<Shop> shopList = shopRepository.findByUser(user);
        Shop getShop = shopList.stream().filter(s -> s.equals(shop)).findFirst().get();

        //가져온 shop 내용 수정
        getShop.updateShop(updateRequest);

        // 기존 이미지들 가져와서 다 지우고 다시 받게 프리사이드 URL 발급
        List<String> imageKeys = imageRepository.findByShopImage(getShop.getId());
        if (!imageKeys.isEmpty()) {
            preSignedProvider.deleteObjects(imageKeys);
            //db에 내용도 다 날리게
            List<Image> imagesDB = imageRepository.findByShopId(getShop.getId());
            imageRepository.deleteAll(imagesDB);
        }

        //새롭게 프리사이드 URL 발급
        return preSignedProvider.createShopUploadUrls(updateRequest.imageCount(), getShop.getId());
    }

    public Shop shopFind(Long shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }

    @Transactional(readOnly = true)
    //TODO 조건에 맞춰서 shop들 검색 (void 아님 귀찬항서)
    public void getShops(ShopLocationSearchParam param, String sort, Long cursor, int size) {

        double latitude = param.latitude();
        double longitude = param.longitude();
        double radius = param.radius();
        List<FoodCategory> foodCategories = param.category();

        //shopRepository.(latitude,longitude,radius,foodCategories,sort,cursor,size+1);

    }

    @Transactional(readOnly = true)
    public List<Shop> findByOwnerId(Long ownerId) {
        return shopRepository.findByUserId(ownerId);
    }
}
