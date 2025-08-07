package shop.matjalalzz.shop.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.shop.dao.ShopQueryDslRepository;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ApproveRequest;
import shop.matjalalzz.shop.dto.GetAllPendingShopListResponse;
import shop.matjalalzz.shop.dto.OwnerShopItem;
import shop.matjalalzz.shop.dto.OwnerShopsList;
import shop.matjalalzz.shop.dto.ShopAdminDetailResponse;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.dto.ShopsResponse;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopListSort;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final static int EARTH_RADIUS = 6371000; // meters

    private final ShopRepository shopRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final PreSignedProvider preSignedProvider;
    private final UserRepository userRepository;
    private final ShopQueryDslRepository shopQueryDslRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    // 관리자가 등록을 원하는 상점들 리스트를 전부 가져옴
    @Transactional(readOnly = true)
    public GetAllPendingShopListResponse adminGetAllPendingShop() {
        List<Shop> shopList = shopRepository.findByApprove(Approve.PENDING);
        return ShopMapper.getAllPendingShopResponse(shopList);

    }

    @Transactional
    public void approve(long shopId, ApproveRequest approveRequest) {
        Approve approve = approveRequest.approve();

        Shop shop = shopFind(shopId);
        shop.updateApprove(approve);

        // approved 시 사용자의 권한도 식당 사장인 OWNER로 바꿔줘야 함
        if (approve == Approve.APPROVED) {
            User owner = shop.getUser();
            owner.updateRole(Role.OWNER);
        }

    }


    // 관리자가 상점에 대한 상세 정보를 보는 용도 (상점에 상태와 관계 없이 다 가져옴)
    @Transactional(readOnly = true)
    public ShopAdminDetailResponse adminGetShop(long shopId) {

        return ShopMapper.shopToShopAdminDetailResponse(shopQueryDslRepository.adminFindShop(shopId), BASE_URL);

//        Shop ownerShop = shopFind(shopId);
//        User owner = ownerShop.getUser();
//        List<String> images = imageRepository.findByShopImage(shopId);
//          return ShopMapper.shopToShopAdminDetailResponse(ownerShop, images, owner, BASE_URL);

    }


    @Transactional
    public PreSignedUrlListResponse newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userFind(userId);

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

        // 등록 된 상태인 식당들만 가져와서 보여줌
        Shop shop = shopRepository.findByIdAndApprove(shopId, Approve.APPROVED)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        log.info("상점 상세 조회 shopId = {}, shopName = {}", shopId, shop.getShopName());

        List<String> imageUrllList = Optional.ofNullable(
                imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewRepository.findReviewCount(shop.getId());

        return ShopMapper.shopDetailResponse(shop, imageUrllList, reviewCount);

    }

    // 사장 한명이 가진 식당 리스트들 조회
    @Transactional(readOnly = true)
    public OwnerShopsList getOwnerShopList(Long userId) {

        // 승인 된 자신의 식당들 리스트를 가져옴
        List<Shop> shopList = shopRepository.findByUserId(userId);

        List<OwnerShopItem> shops = shopList.stream().map(shop ->
            {
                String image =
                    BASE_URL + imageRepository.findByShopId(shop.getId()).stream().findFirst().get()
                        .getS3Key();
                return ShopMapper.shopToOwnerShopItem(shop, image);
            }
        ).toList();

        return new OwnerShopsList(shops);
    }


    @Transactional(readOnly = true)
    // 사장이 자신의 shop 하나를 상세 조회
    public ShopOwnerDetailResponse getOwnerShop(Long shopId, Long userId) {

        // 해당 상점이 없으면 에러
        Shop shop = ownerShopFind(shopId, userId);

        //사진 리스트로 가져왔을 때 없어도 에러는 반환 X
        List<String> imageUrlList = Optional.ofNullable(
                imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewRepository.findReviewCount(shop.getId());

        return ShopMapper.shopOwnerDetailResponse(shop, imageUrlList, reviewCount);

    }


    // shop 수정
    @Transactional
    public PreSignedUrlListResponse editShop(Long shopId, long userId, ShopUpdateRequest updateRequest) {

        User user = userFind(userId);

        Shop getShop = ownerShopFind(shopId, userId);

        ShopUpdateVo shopUpdateVo = ShopMapper.updateToShop(updateRequest);

        //가져온 shop 내용 수정
        getShop.updateShop(shopUpdateVo, user);

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

    public User userFind(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 승인 된 식당만 조회, 수정 가능
    public Shop ownerShopFind(Long shopId, Long userId) {
        return shopRepository.findByIdAndUserId(shopId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }


    @Transactional(readOnly = true)
    public ShopsResponse getShops(ShopLocationSearchParam param, String sort, Double distanceOrRating, int size, Long shopId) {
        log.info("상점 목록 조회");
        double latitude = param.latitude() != null ? param.latitude() : 37.5724; // 기본 좌표값은 종로
        double longitude = param.longitude() != null ? param.longitude() : 126.9794;
        double radius = param.radius() != null ? param.radius() : 3000.0;  // 3km
        List<FoodCategory> foodCategories =
            (param.category() != null && !param.category().isEmpty()) ? param.category()
                : List.of(FoodCategory.values());

        List<ShopsItem> allShopItems = shopQueryDslRepository.findAllShops(latitude, longitude,
            radius, foodCategories, distanceOrRating, size, sort, shopId);

        //이미지만 또 따로 필요하므로 재조합을 해줘야 함
        List<ShopsItem> shopsItemStream = allShopItems.stream().map(item -> {
                String thumnail = imageRepository.findFirstByShopId(item.shopId()).map(
                    Image::getS3Key).orElse(null);

                return new ShopsItem(
                    item.shopId(),
                    item.shopName(),
                    item.category(),
                    item.roadAddress(),
                    item.detailAddress(),
                    item.latitude(),
                    item.longitude(),
                    item.rating(),
                    BASE_URL + thumnail,
                    item.distance() //마지막 거리 차이값을 cursor 용도로 쓰기 위해
                );
            }
        ).collect(Collectors.toCollection(ArrayList::new));

        boolean next = shopsItemStream.size() > size;
        ShopsItem last = null;


        if (next){
            last = shopsItemStream.remove(size);  //위에서 size+1로 가져온 값들 중 마지막 값을 가져옴
        }
        else {
            distanceOrRating=null;
            shopId=null;
        }

        if (last != null) {
            if ("rating".equals(sort)){
                distanceOrRating = last.rating();
                shopId = last.shopId();
            }
            else {
                distanceOrRating =  last.distance();
                shopId = last.shopId();
            }
        }


        return ShopsResponse.builder()
            .distanceOrRating(distanceOrRating)
            .shopId(shopId)
            .content(shopsItemStream)
            .build();
    }


// 기본 JPA 쿼리로 속도 테스트 후 지울 예정

//        switch (sort) {
//            //별점순
//            case "rating" -> {
//                Double ratingCursor = cursor != null ? cursor.doubleValue() : 5.0; //별점이 높은 순으로 가져오니 max
//                Slice<Shop> shopSlice = shopRepository.findByRatingCursorAndApprove(
//                    latitude, longitude, radius, foodCategories, ratingCursor, Approve.APPROVED,
//                    PageRequest.of(0, size)
//                );
//
//                //Long nextCursor = null;
//                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
//                    nextCursor = shopSlice.getContent().getLast().getRating().longValue();
//                }
//
//                return ShopsResponse.builder()
//                    .nextCursor(nextCursor)
//                    .content(toShopsItems(shopSlice))
//                    .build();
//            }
//
//            // 거리순
//            case "distance" -> {
//                Slice<Shop> shopSlice = shopRepository.findByDistanceAndApprove(
//                    latitude, longitude, radius, Approve.APPROVED, foodCategories, cursor, PageRequest.of(0, size)
//                );
//
//                Long nextCursor = null;
//                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
//                    Shop last = shopSlice.getContent().getLast();
//                    double lastDistance = calculateDistanceInMeters(latitude, longitude, last.getLatitude(), last.getLongitude());
//                    if (lastDistance < radius) {        //계산 돌려본 결과 좌표값이 radius 값보다 크면 null
//                        nextCursor = (long) lastDistance;
//                    }
//                }
//
//                return ShopsResponse.builder()
//                    .nextCursor(nextCursor)
//                    .content(toShopsItems(shopSlice))
//                    .build();
//            }
//
//            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
//        }
//    }


    @Transactional(readOnly = true)
    public ShopPageResponse getShopList(String query, ShopListSort sort, String cursor, int size) {
        return switch (sort) {
            case ShopListSort.RATING -> getShopListByRating(query, cursor, size);
            case ShopListSort.CREATED_AT -> getShopListByCreatedAt(query, cursor, size);
            case ShopListSort.NAME -> getShopListByName(query, cursor, size);
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
        };
    }

    private ShopPageResponse getShopListByRating(String query, String cursor, int size) {
        Double ratingCursor = null;
        if (cursor != null) {
            //파싱 불가능 검증
            try {
                ratingCursor = Double.parseDouble(cursor);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
            }
        }

        Slice<Shop> result = shopRepository.findCursorListByRating(
            ratingCursor, query, Approve.APPROVED, PageRequest.of(0, size));
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getRating());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId());
            if (images.isEmpty()) {
                return null;
            }
            String s3key = images.getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }

    private ShopPageResponse getShopListByCreatedAt(String query, String cursor, int size) {
        LocalDateTime timeCursor = null;
        if (cursor != null) {
            // 파싱 불가능 검증
            try {
                timeCursor = LocalDateTime.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
            }
        }

        Slice<Shop> result = shopRepository.findCursorListByCreatedAt(
            timeCursor, query, Approve.APPROVED, PageRequest.of(0, size));
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getCreatedAt());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId());
            if (images.isEmpty()) {
                return null;
            }
            String s3key = images.getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }

    @Transactional(readOnly = true)
    public List<Shop> findByOwnerId(Long ownerId) {
        return shopRepository.findByUserId(ownerId);
    }

    private ShopPageResponse getShopListByName(String query, String cursor, int size) {
        Slice<Shop> result = shopRepository.findCursorListByName(
            cursor, query, Approve.APPROVED, PageRequest.of(0, size));
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getShopName());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId());
            if (images.isEmpty()) {
                return null;
            }
            String s3key = images.getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }

    private List<ShopsItem> toShopsItems(Slice<Shop> shopSlice) {
        return shopSlice.getContent().stream()
            .map(shop -> {
                String thumbnailUrl = imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId())
                    .stream()
                    .findFirst()
                    .map(image -> BASE_URL + image.getS3Key())
                    .orElse(null);
                return ShopMapper.sliceShopToShopsItem(shop, thumbnailUrl);

            })
            .toList();
    }


    /* 레포지토리에서는 거리 계산 결과(숫자)를 반환해주지 않기 때문에 응답에 포함할 다음 커서 거리 값을 계산하기 위해
       마지막 Shop의 좌표를 기준으로 사용자의 거리값을 계산해 커서로 넘겨야 함,
       다음 커서(nextCursor) 값을 정확하게 계산해서 프론트에 넘기기 위해, 자바 코드에서 최종적으로 한 번 더 거리 계산을 수행 */
    private static double calculateDistanceInMeters(
        double lat1, double lon1,
        double lat2, double lon2
    ) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

}
