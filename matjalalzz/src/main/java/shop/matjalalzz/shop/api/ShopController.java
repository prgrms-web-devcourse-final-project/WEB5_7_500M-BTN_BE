package shop.matjalalzz.shop.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;

@RestController
@RequiredArgsConstructor
@Tag(name = "식당 API", description = "식당 관련 API")
public class ShopController {
    private final ShopService shopService;

    @Operation(summary = "식당 생성", description = "새로운 식당을 생성합니다.")
    @PostMapping("/shops/presigned-urls")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<PreSignedUrlResponse> createShop(@RequestBody ShopCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        PreSignedUrlResponse preSignedUrlResponse = shopService.newShop(principal.getId(), request);
        return BaseResponse.ok(preSignedUrlResponse,BaseStatus.CREATED);
    }


    @Operation(summary = "식당 상세 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    @GetMapping("/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopDetailResponse> getDetailShop(@PathVariable Long shopId) {
        ShopDetailResponse response = shopService.getShop(shopId);
        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(summary = "사장의 식당 상세 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    @GetMapping("/owner/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopOwnerDetailResponse> getDetailShopOwner(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal) {
        ShopOwnerDetailResponse response = shopService.getShopOwner(shopId, principal.getId());
        return BaseResponse.ok(response, BaseStatus.OK);
    }


    @Operation(summary = "사장 식당 정보 수정", description = "식당 정보를 수정합니다.")
    @PatchMapping("/owner/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PreSignedUrlResponse> updateShop(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal,
        @RequestBody @Valid ShopUpdateRequest shopUpdateRequest) {
        PreSignedUrlResponse urlResponse = shopService.editShop(shopId, principal.getId(), shopUpdateRequest);
        return BaseResponse.ok(urlResponse, BaseStatus.OK);
    }


    @Operation(summary = "식당 목록 조회", description = "위치 기반으로 식당 목록을 조회합니다.")
    @GetMapping("/shops")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopPageResponse> getShops(ShopLocationSearchParam param,
        @RequestParam(defaultValue = "distance") String sort, //정렬 기준(근처순, 예약많은순, 평점순) (근처순이 기본(distance))
        @RequestParam(defaultValue = "0") Long cursor,
        @RequestParam(defaultValue = "10") int size) {

        shopService.getShops(param,sort,cursor,size);
        return BaseResponse.ok(ShopPageResponse.builder().build(), BaseStatus.OK);
    }

    @Operation(summary = "식당 검색", description = "키워드로 식당을 검색합니다.")
    @GetMapping("/shops/search")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopPageResponse> getShopsBySearch(@RequestParam String query,
        @RequestParam String sort,
        @RequestParam(defaultValue = "0") Long cursor,
        @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.ok(ShopPageResponse.builder().build(), BaseStatus.OK);
    }


}
