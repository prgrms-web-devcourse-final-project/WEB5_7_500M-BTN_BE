package shop.matjalalzz.shop.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopResponse;
import shop.matjalalzz.user.app.UserService;

@RestController
@RequiredArgsConstructor
@Tag(name = "상점 API", description = "상점 관련 API")
public class ShopController {
    private final ShopService shopService;

    @Operation(summary = "상점 생성", description = "새로운 상점을 생성합니다.")
    @PostMapping("/shops")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<Void> createShop(@RequestBody ShopCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        shopService.newShop(principal.getId(), request);
        return BaseResponse.ok(BaseStatus.CREATED);
    }


    @Operation(summary = "상점 상세 조회", description = "특정 상점의 상세 정보를 조회합니다.")
    @GetMapping("/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopResponse> getShop(@PathVariable Long shopId) {

        ShopResponse response = shopService.getShop(shopId);
        return BaseResponse.ok(response, BaseStatus.OK);
    }



    @Operation(summary = "상점 목록 조회", description = "위치 기반으로 상점 목록을 조회합니다.")
    @GetMapping("/shops")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopPageResponse> getShops(ShopLocationSearchParam param,
        @RequestParam(defaultValue = "distance") String sort,
        @RequestParam(defaultValue = "0") Long cursor,
        @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.ok(ShopPageResponse.builder().build(), BaseStatus.OK);
    }

    @Operation(summary = "상점 검색", description = "키워드로 상점을 검색합니다.")
    @GetMapping("/shops/search")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopPageResponse> getShopsBySearch(@RequestParam String query,
        @RequestParam String sort,
        @RequestParam(defaultValue = "0") Long cursor,
        @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.ok(ShopPageResponse.builder().build(), BaseStatus.OK);
    }

    @Operation(summary = "점주용 상점 조회", description = "점주가 자신의 상점 정보를 조회합니다.")
    @GetMapping("/owner/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopOwnerResponse> getShopOwner(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal) {
//        shopService.getOwnerShop(shopId, principal.getId());
        return BaseResponse.ok(ShopOwnerResponse.builder().build(), BaseStatus.OK);
    }

    @Operation(summary = "상점 정보 수정", description = "상점 정보를 수정합니다.")
    @PatchMapping("/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> updateShop(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal) {
        return BaseResponse.ok(BaseStatus.OK);
    }
}
