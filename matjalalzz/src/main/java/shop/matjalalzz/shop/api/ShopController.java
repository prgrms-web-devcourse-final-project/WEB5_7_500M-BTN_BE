package shop.matjalalzz.shop.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.entity.Shop;

@RestController
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/shops")
    public BaseResponse<Void> post(@RequestBody ShopCreateRequest shopCreateRequest,
        @AuthenticationPrincipal PrincipalUser principal) {
        shopService.newShop(principal.getId(), shopCreateRequest);

    }



}
