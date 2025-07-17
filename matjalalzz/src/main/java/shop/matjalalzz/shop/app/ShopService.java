package shop.matjalalzz.shop.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.OwnerShopItem;
import shop.matjalalzz.shop.dto.OwnerShopsList;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.dto.ShopsResponse;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopListSort;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final static int EARTH_RADIUS = 6371000; // meters

    private final ShopRepository shopRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;

    private final PreSignedProvider preSignedProvider;
    private final UserRepository userRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public PreSignedUrlListResponse newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userFind(userId);

        Shop newShop = ShopMapper.createToShop(shopCreateRequest, user);

        // 점주가 한명이며 한명이 식당 여러개 등록이 가능해도 식당 주소는 다 달라야 하며 or 사업자 등록번호가 이미 있으면 에러
        shopRepository.findByBusinessCodeOrRoadAddressAndDetailAddress(newShop.getBusinessCode(), newShop.getRoadAddress(), newShop.getDetailAddress())
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
        Shop shop = shopFind(shopId);

        List<String> imageUrllList = Optional.ofNullable(imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewRepository.findReviewCount(shop.getId());

        return ShopMapper.shopDetailResponse(shop, imageUrllList, reviewCount);

    }

    // 사장 한명이 가진 식당 리스트들 조회
    @Transactional(readOnly = true)
    public OwnerShopsList getOwnerShopList (Long userId){

        List<Shop> shopList = shopRepository.findByUser(userFind(userId));

        List<OwnerShopItem> shops = shopList.stream().map(shop ->
            {
                String image = BASE_URL + imageRepository.findByShopId(shop.getId()).stream().findFirst().get().getS3Key();
                return ShopMapper.shopToOwnerShopItem(shop, image);
            }
        ).toList();

        return new OwnerShopsList(shops);
    }



    @Transactional(readOnly = true)
    // 사장이 자신의 shop을 조회 한 경우 조회 허용
    public ShopOwnerDetailResponse getOwnerShop(Long shopId, Long userId) {

        // 해당 상점이 없으면 에러
        Shop shop = ownerShopFind(shopId, userId);

        //사진 리스트로 가져왔을 때 없어도 에러는 반환 X
        List<String> imageUrlList = Optional.ofNullable(imageRepository.findByShopIdOrderByImageIndexAsc(shop.getId()))
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
        return shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }
    public User userFind(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Shop ownerShopFind(Long shopId, Long userId) {
        return shopRepository.findByIdAndUserId(shopId, userId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));
    }



    @Transactional(readOnly = true)
    public ShopsResponse getShops(ShopLocationSearchParam param, String sort, Long cursor, int size) {
        double latitude = param.latitude() != null ? param.latitude() : 37.5724; // 기본 좌표값은 종로
        double longitude = param.longitude() != null ? param.longitude() : 126.9794;
        double radius = param.radius() != null ? param.radius() : 3000.0;  // 3km
        List<FoodCategory> foodCategories = (param.category() != null && !param.category().isEmpty()) ? param.category()
            : List.of(FoodCategory.values());


        switch (sort) {
            case "rating" -> {
                Double ratingCursor = cursor != null ? cursor.doubleValue() : 5.0; //별점이 높은 순으로 가져오니 max
                Slice<Shop> shopSlice = shopRepository.findByRatingCursor(
                    latitude, longitude, radius, foodCategories, ratingCursor, PageRequest.of(0, size)
                );

                Long nextCursor = null;
                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
                    nextCursor = shopSlice.getContent().getLast().getRating().longValue();
                }

                return ShopsResponse.builder()
                    .nextCursor(nextCursor)
                    .content(toShopsItems(shopSlice))
                    .build();
            }

            case "distance" -> {
                Slice<Shop> shopSlice = shopRepository.findByDistance(
                    latitude, longitude, radius, foodCategories, cursor, PageRequest.of(0, size)
                );

                Long nextCursor = null;
                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
                    Shop last = shopSlice.getContent().getLast();
                    double lastDistance = calculateDistanceInMeters(latitude, longitude, last.getLatitude(), last.getLongitude());
                    if (lastDistance < radius) {        //계산 돌려본 결과 좌표값이 radius 값보다 크면 null
                        nextCursor = (long) lastDistance;
                    }
                }

                return ShopsResponse.builder()
                    .nextCursor(nextCursor)
                    .content(toShopsItems(shopSlice))
                    .build();
            }

            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
        }
    }



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
            ratingCursor, query, PageRequest.of(0, size));
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
            timeCursor, query, PageRequest.of(0, size));
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
            cursor, query, PageRequest.of(0, size));
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
