package shop.matjalalzz.shop.app;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import shop.matjalalzz.image.app.ImageFacade;
import shop.matjalalzz.image.app.commend.ImageCommendService;
import shop.matjalalzz.image.app.query.ImageQueryService;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.review.app.ReviewQueryService;
import shop.matjalalzz.shop.app.commend.ShopCommendService;
import shop.matjalalzz.shop.app.query.ShopQueryService;
import shop.matjalalzz.shop.dto.AdminFindShopInfo;
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
import shop.matjalalzz.shop.dto.projection.OwnerShopProjection;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopListSort;
import shop.matjalalzz.shop.mapper.ShopMapper;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopFacade {
    private final ShopCommendService shopCommendService;
    private final ShopQueryService shopQueryService;
    private final PreSignedProvider preSignedProvider;
    private final ReviewQueryService reviewQueryService;
    private final UserService userService;
    private final ImageQueryService imageQueryService;
    private final ImageCommendService imageCommendService;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    // 관리자가 등록을 원하는 상점들 리스트를 전부 가져옴
    public GetAllPendingShopListResponse adminFindAllPendingShop() {
        List<Shop> shopList = shopQueryService.adminFindAllPendingShop();
        return ShopMapper.getAllPendingShopResponse(shopList);
    }

    // 식당 상태 변경
    @Transactional
    public void approveUpdate(long shopId, ApproveRequest approveRequest){
        Shop shop = shopCommendService.approveUpdate(shopId, approveRequest);
        // approved 시 사용자의 권한도 식당 사장인 OWNER로 바꿔줘야 함
        if (approveRequest.approve() == Approve.APPROVED) {
            User owner = shop.getUser();
            owner.updateRole(Role.OWNER);
        }
    }

    // 관리자가 상점을 조회
    public ShopAdminDetailResponse adminGetShop(long shopId) {
        AdminFindShopInfo shopInfo = shopQueryService.adminGetShop(shopId);
        return ShopMapper.shopToShopAdminDetailResponse(shopInfo, BASE_URL);
    }

    // shop 생성
    @Transactional
    public PreSignedUrlListResponse createNewShop(long userId, ShopCreateRequest shopCreateRequest) {
        //facade 방식 필요
        User user = userService.getUserById(userId);
        Shop newShop = ShopMapper.createToShop(shopCreateRequest, user);

        shopQueryService.newShopCheck(newShop.getBusinessCode(), newShop.getRoadAddress(), newShop.getDetailAddress())
            .ifPresent(shop -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_SHOP);
                });
        shopCommendService.createNewShop(newShop);

        return preSignedProvider.createShopUploadUrls(shopCreateRequest.imageCount(), newShop.getId());
    }


    // 등록 된 상태인 식당에 상세 정보
    public ShopDetailResponse findShopDetail(long shopId) {
        Shop shop = shopQueryService.findOneShop(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));
        log.info("상점 상세 조회 shopId = {}, shopName = {}", shopId, shop.getShopName());
        List<String> imageUrllList = Optional.ofNullable(imageQueryService.findByShopAndImage(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();
        //리뷰 갯수가 몇개인지 보내줘야 함 (reviewFacade에서 호출하면 순환참조 발생)
        int reviewCount = reviewQueryService.findReviewCountByShop(shop.getId());

        return ShopMapper.shopDetailResponse(shop, imageUrllList, reviewCount);
    }

    public ShopOwnerDetailResponse findOwnerShopDetail(long shopId, long ownerId) {
        Shop shop = shopQueryService.findOwnerShop(shopId, ownerId);
        List<String> imageUrlList = Optional.ofNullable(
                imageQueryService.findByShopAndImage(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        //리뷰 갯수가 몇개인지 보내줘야 함
        int reviewCount = reviewQueryService.findReviewCountByShop(shop.getId());

        return ShopMapper.shopOwnerDetailResponse(shop, imageUrlList, reviewCount);
    }

    // 사장이 가진 식당들 전부 조회
    public OwnerShopsList findOwnerShopList(long userId) {
        List<OwnerShopProjection> rows = shopQueryService.findOwnerShopList(userId);
        List<OwnerShopItem> shopItems = rows.stream()
            .map(r -> ShopMapper.ownerRowToItem(r, BASE_URL)).toList();
        return new OwnerShopsList(shopItems);
    }

    // 식당 수정
    @Transactional
    public PreSignedUrlListResponse editShop(Long shopId, long userId, ShopUpdateRequest updateRequest) {
        User user = userService.getUserById(userId);
        Shop shop = shopQueryService.findOwnerShop(shopId, user.getId());
        ShopUpdateVo shopUpdateVo = ShopMapper.updateToShop(updateRequest);

        shopCommendService.editShop(shop,shopUpdateVo,user);

        // 기존 이미지들 가져와서 다 지우고 다시 받게 프리사이드 URL 발급
        List<String> imageKeys = imageQueryService.findByShopAndImageKey(shop.getId());
        if (!imageKeys.isEmpty()) {
            preSignedProvider.deleteObjects(imageKeys);
            //db에 내용도 다 날리게
            List<Image> imagesDB = imageQueryService.findByShopAndImage(shop.getId());
            imageCommendService.deleteAllImages(imagesDB);
        }
        //새롭게 프리사이드 URL 발급
        return preSignedProvider.createShopUploadUrls(updateRequest.imageCount(), shop.getId());
    }


    // 거리나 별점순으로 사용자가 상점 조회
    public ShopsResponse findDistanceOrRatingShops(ShopLocationSearchParam param, String sort, Double distanceOrRating, int size, Long shopId) {
        log.info("상점 목록 조회");
        double latitude = param.latitude() != null ? param.latitude() : 37.5724; // 기본 좌표값은 종로
        double longitude = param.longitude() != null ? param.longitude() : 126.9794;
        double radius = param.radius() != null ? param.radius() : 3000.0;  // 3km
        List<FoodCategory> foodCategories = (param.category() != null && !param.category().isEmpty()) ? param.category() : List.of(FoodCategory.values());


        List<ShopsItem> allShopItems = shopQueryService.findDistanceOrRatingShops(latitude, longitude, radius, foodCategories, distanceOrRating, size, sort, shopId);

        //이미지만 또 따로 필요하므로 재조합을 해줘야 함

        List<ShopsItem> shopsItemStream = allShopItems.stream().map(item -> {
                Optional<Image> shopThumbnail = imageQueryService.findShopThumbnail(item.shopId());
                String thumbnail = shopThumbnail.map(image -> BASE_URL + image.getS3Key()).orElse(null);

                return new ShopsItem(
                item.shopId(),
                item.shopName(),
                item.category(),
                item.roadAddress(),
                item.detailAddress(),
                item.latitude(),
                item.longitude(),
                item.rating(),
                thumbnail,
                item.distance() //마지막 거리 차이값을 cursor 용도로 쓰기 위해
            );
        }
        ).collect(Collectors.toCollection(ArrayList::new));

        boolean next = shopsItemStream.size() > size;
        ShopsItem last = null;

        if (next) {
            last = shopsItemStream.remove(size);  //위에서 size+1로 가져온 값들 중 마지막 값을 가져옴
        } else {
            distanceOrRating = null;
            shopId = null;
        }

        if (last != null) {
            if ("rating".equals(sort)) {
                distanceOrRating = last.rating();
                shopId = last.shopId();
            } else {
                distanceOrRating = last.distance();
                shopId = last.shopId();
            }
        }

        return ShopsResponse.builder()
            .distanceOrRating(distanceOrRating)
            .shopId(shopId)
            .content(shopsItemStream)
            .build();
    }


    public ShopPageResponse getShopList(String query, ShopListSort sort, String cursor, int size) {
        return switch (sort) {
            case ShopListSort.RATING -> getShopListByRating(query, cursor, size);
            case ShopListSort.CREATED_AT -> getShopListByCreatedAt(query, cursor, size);
            case ShopListSort.NAME -> getShopListByName(query, cursor, size);
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

        Slice<Shop> result = shopQueryService.findShopCursorList(ratingCursor, query, Approve.APPROVED, PageRequest.of(0, size), ShopListSort.RATING);
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getRating());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = s.getImages();
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

        Slice<Shop> result = shopQueryService.findShopCursorList(timeCursor, query, Approve.APPROVED, PageRequest.of(0, size), ShopListSort.CREATED_AT);
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getCreatedAt());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = s.getImages();
            if (images.isEmpty()) {
                return null;
            }
            String s3key = images.getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }



    private ShopPageResponse getShopListByName(String query, String cursor, int size) {
        Slice<Shop> result = shopQueryService.findShopCursorList(cursor, query, Approve.APPROVED, PageRequest.of(0, size), ShopListSort.NAME);
        String nextCursor = null;
        if (result.hasNext()) {
            nextCursor = String.valueOf(result.getContent().getLast().getShopName());
        }
        List<String> thumbnailList = result.getContent().stream().map(s -> {
            List<Image> images = s.getImages();
            if (images.isEmpty()) {
                return null;
            }
            String s3key = images.getFirst().getS3Key();
            return BASE_URL + s3key;
        }).toList();
        return ShopMapper.toShopPageResponse(nextCursor, result.getContent(), thumbnailList);
    }







}

