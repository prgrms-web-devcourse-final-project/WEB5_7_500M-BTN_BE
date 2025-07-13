package shop.matjalalzz.shop.app;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
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
    private final ImageRepository imageRepository;

    private final PreSignedProvider preSignedProvider;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public PreSignedUrlResponse newShop(long userId, ShopCreateRequest shopCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Shop newShop = ShopMapper.createToShop(shopCreateRequest,user);

        // 점주가 한명이며 한명이 식당 여러개 등록이 가능해도 식당 주소는 다 달라야 하며 or 사업자 등록번호가 이미 있으면 에러
        shopRepository.findByBusinessCodeOrRoadAddress(newShop.getBusinessCode(), newShop.getRoadAddress())
            .ifPresent(shop ->  {throw new BusinessException(ErrorCode.DUPLICATE_SHOP); });

        shopRepository.save(newShop);
        // 프리사이드 url 링크 반환
        return preSignedProvider.generatePresignedUrl(newShop.getShopName(), newShop.getImageCount(), newShop.getId());

    }



    // 사장이 자신의 shop을 조회 한 경우 수정 허용되게
    public ShopResponse getShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_SHOP));

        //사진 리스트로 가져왔을 때 없어도 에러는 반환 X    이거 mapper로 이동해야 함
        List<String> imageUrllList = Optional.ofNullable(imageRepository.findByShopId(shop.getId()))
            .orElse(List.of())
            .stream()
            .map(image -> BASE_URL + image.getS3Key()).toList();

        return ShopMapper.shopDetailResponse(shop, imageUrllList);

    }

}
