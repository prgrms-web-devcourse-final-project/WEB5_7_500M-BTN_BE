package shop.matjalalzz.shop.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.dto.ApproveRequest;
import shop.matjalalzz.shop.dto.GetAllPendingShopListResponse;
import shop.matjalalzz.shop.dto.OwnerShopsList;
import shop.matjalalzz.shop.dto.ShopAdminDetailResponse;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopLocationSearchParam;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.dto.ShopsResponse;
import shop.matjalalzz.shop.entity.ShopListSort;

@RestController
@RequiredArgsConstructor
@Tag(name = "식당 API", description = "식당 관련 API")
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "관리자가 pending 상태인 식당들 리스트를 가져옴", description = "관리자는 등록을 원하는 식당 리스트를 볼 수 있습니다. (Completed)")
    @GetMapping("/admin/shops")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<GetAllPendingShopListResponse> getPendingShop() {
        return BaseResponse.ok(shopService.adminGetAllPendingShop(), BaseStatus.OK);
    }

    @Operation(summary = "관리자가 식당 등록에 대한 요청을 승인 또는 거절 ", description = "APPROVED(승인) 또는 REJECTED(거절) (Completed)")
    @PostMapping("/admin/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public void getPendingShop(@PathVariable Long shopId,
        @RequestBody @Valid ApproveRequest approveRequest) {
        shopService.approve(shopId, approveRequest);
    }


    @Operation(summary = "관리자가 식당에 대한 정보를 봄", description = "관리자는 식당에 대한 정보를 볼 수 있습니다. (식당의 등록 상태가 뭐든 볼 수 있음) (Completed)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/admin/shops/{shopId}")
    public BaseResponse<ShopAdminDetailResponse> getShopAdminDetail(@PathVariable Long shopId) {
        return BaseResponse.ok(shopService.adminGetShop(shopId), BaseStatus.OK);
    }

    @Operation(summary = "식당 생성", description = """
        사용자는 새로운 식당을 생성합니다.(Completed)
        
        사진 전송 시 헤더에
        Cache-Control 값이 no-cache,no-store,must-revalidate 되어 있어야 합니다
        
        """)
    @PostMapping("/shops/presigned-urls")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<PreSignedUrlListResponse> createShop(
        @RequestBody @Valid ShopCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        PreSignedUrlListResponse preSignedUrlListResponse = shopService.newShop(principal.getId(),
            request);
        return BaseResponse.ok(preSignedUrlListResponse, BaseStatus.CREATED);
    }


    @Operation(summary = "식당 상세 조회", description = "사용자가 특정 식당의 상세 정보를 조회합니다. 식당 등록 상태가 APPROVED인 식당들만 조회 가능 (Completed)")
    @GetMapping("/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopDetailResponse> getDetailShop(@PathVariable Long shopId) {
        ShopDetailResponse response = shopService.getShop(shopId);
        return BaseResponse.ok(response, BaseStatus.OK);
    }


    @Operation(summary = "사장이 가진 식당들 조회", description = "사장 한명이 가진 식당들 리스트들을 조회합니다. 승인 여부 상태와 상관없이 조회 (Completed)")
    @GetMapping("/owner/shops")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<OwnerShopsList> getOwnerShops(
        @AuthenticationPrincipal PrincipalUser principal) {
        OwnerShopsList response = shopService.getOwnerShopList(principal.getId());
        return BaseResponse.ok(response, BaseStatus.OK);
    }


    @Operation(summary = "사장의 식당 상세 조회", description = "자신이 가진 식당의 상세 정보를 조회합니다. (Completed)")
    @GetMapping("/owner/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopOwnerDetailResponse> getDetailShopOwner(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal) {
        ShopOwnerDetailResponse response = shopService.getOwnerShop(shopId, principal.getId());
        return BaseResponse.ok(response, BaseStatus.OK);
    }


    @Operation(summary = "사장 식당 정보 수정", description = "자신이 가진 식당 정보를 수정합니다. (Completed)")
    @PutMapping("/owner/shops/{shopId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PreSignedUrlListResponse> updateShop(@PathVariable Long shopId,
        @AuthenticationPrincipal PrincipalUser principal,
        @RequestBody @Valid ShopUpdateRequest shopUpdateRequest) {
        PreSignedUrlListResponse urlResponse = shopService.editShop(shopId, principal.getId(),
            shopUpdateRequest);
        return BaseResponse.ok(urlResponse, BaseStatus.OK);
    }


    @Operation(summary = "식당 목록 조회", description = """
        일반 사용자가 위치 기반으로 식당 목록을 조회합니다. (식당 상태가 APPROVED인 식당들만 조회 가능) (Completed)
        
        반경은 m 단위로 주시면 되며 ->  3km (3000)
        만약 사용자가 자신의 위치를 허용한다면 latitude과 longitude에 사용자 위도 경도, 원하는 범위를 넣어서 요청을 보내고 그렇지 않는다면 기본값으론 종로구 좌표에 3km 범위 식당을 가져옵니다
        
        예시 /shops?radius=1000000&sort=rating
        
        | 필드 명     | 자료형  | 필수 여부 | 설명                   | 기본값           |
        |------------|---------|-----------|-------------------------|------------------|
        | latitude   | double  | Required  | 사용자 위치의 위도         | 37.5724          |
        | longitude  | double  | Required  | 사용자 위치의 경도         | 126.9794         |
        | radius     | double  | Optional  | 검색 반경 (단위: m)       | 3000.0           |
        | category   | string  | Optional  | 음식 카테고리             | 전체             |
        | sort       | string  | Optional  | 정렬 기준 (근처순, 평점순) | 근처 순(distance) |
        
        """)
    @GetMapping("/shops")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopsResponse> getShops(
        @ParameterObject ShopLocationSearchParam param,
        @RequestParam(defaultValue = "distance") String sort, //정렬 기준(근처순, 평점순) (근처순이 기본(distance))
        @RequestParam(required = false) Double cursor,
        @RequestParam(defaultValue = "10") int size) {

        ShopsResponse shops = shopService.getShops(param, sort, cursor, size);
        return BaseResponse.ok(shops, BaseStatus.OK);
    }

    @Operation(summary = "식당 검색", description = "키워드로 식당을 검색합니다."
        + "정렬기준: NAME, CREATED_AT, RATING  "
        + "(식당 상태가 APPROVED인 식당들만 조회 가능)" + "(Completed)")
    @GetMapping("/shops/search")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ShopPageResponse> getShopsBySearch(
        @RequestParam(required = false) String query,
        @RequestParam(defaultValue = "CREATED_AT") ShopListSort sort,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.ok(shopService.getShopList(query, sort, cursor, size), BaseStatus.OK);
    }


}
