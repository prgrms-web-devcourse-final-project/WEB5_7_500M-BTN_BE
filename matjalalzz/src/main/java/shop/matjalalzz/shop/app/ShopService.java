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
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.dto.ShopsResponse;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final static int EARTH_RADIUS = 6371000; // meters

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;

    private final PreSignedProvider preSignedProvider;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public PreSignedUrlListResponse newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
    public ShopsResponse getShops(ShopLocationSearchParam param, String sort, Long cursor,
        int size) {
        double latitude = param.latitude() != null ? param.latitude() : 37.5724; // 기본 좌표값은 종로
        double longitude = param.longitude() != null ? param.longitude() : 126.9794;
        double radius = param.radius() != null ? param.radius() : 3000.0;  // 3km
        List<FoodCategory> foodCategories =
            (param.category() != null && !param.category().isEmpty()) ? param.category()
                : List.of(FoodCategory.values());

        switch (sort) {
            case "rating" -> {
                Double ratingCursor =
                    cursor != null ? cursor.doubleValue() : 5.0; //별점이 높은 순으로 가져오니 max
                Slice<Shop> shopSlice = shopRepository.findByRatingCursor(
                    latitude, longitude, radius, foodCategories, ratingCursor,
                    PageRequest.of(0, size)
                );

                Long nextCursor = null;
                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
                    nextCursor = shopSlice.getContent().getLast().getRating().longValue();
                }

                return ShopsResponse.builder()
                    .nextCursor(nextCursor)
                    .shops(toShopsItems(shopSlice))
                    .build();
            }

            case "distance" -> {
                Slice<Shop> shopSlice = shopRepository.findByDistance(
                    latitude, longitude, radius, foodCategories, cursor, PageRequest.of(0, size)
                );

                Long nextCursor = null;
                if (shopSlice.hasNext() && !shopSlice.isEmpty()) {
                    Shop last = shopSlice.getContent().getLast();
                    double lastDistance = calculateDistanceInMeters(latitude, longitude,
                        last.getLatitude(), last.getLongitude());
                    if (lastDistance < radius) {
                        nextCursor = (long) lastDistance;
                    }
                }

                return ShopsResponse.builder()
                    .nextCursor(nextCursor)
                    .shops(toShopsItems(shopSlice))
                    .build();
            }

            default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sort);
        }
    }

    @Transactional(readOnly = true)
    public ShopPageResponse getShopList(String query, String sort, String cursor, int size) {
        return switch (sort) {
            case "rating" -> getShopListByRating(query, cursor, size);
            case "createdAt" -> getShopListByCreatedAt(query, cursor, size);
            case "name" -> getShopListByName(query, cursor, size);
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
        };
    }

    private ShopPageResponse getShopListByRating(String query, String cursor, int size) {
        Double ratingCursor = null;
        if (cursor != null) {
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
            String s3key = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId()).getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }

    private ShopPageResponse getShopListByCreatedAt(String query, String cursor, int size) {
        LocalDateTime timeCursor = null;
        if (cursor != null) {
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
            nextCursor = String.valueOf(result.getContent().getLast().getRating());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            String s3key = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId()).getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }

    private ShopPageResponse getShopListByName(String query, String cursor, int size) {
        Slice<Shop> result = shopRepository.findCursorListByName(
            cursor, query, PageRequest.of(0, size));
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getRating());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            String s3key = imageRepository.findByShopIdOrderByImageIndexAsc(
                s.getId()).getFirst().getS3Key();
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


    // 응답에 포함할 다음 커서 거리 값을 계산하기 위해 마지막 Shop의 좌표를 기준으로 사용자의 거리값을 계산해 커서로 넘겨야 함
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
