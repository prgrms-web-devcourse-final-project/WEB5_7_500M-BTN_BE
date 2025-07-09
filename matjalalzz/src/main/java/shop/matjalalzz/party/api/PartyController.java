package shop.matjalalzz.party.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("parties")
@Tag(name = "파티 API", description = "파티 관련 API")
public class PartyController {

    private final PartyService partyService;

    @Operation(summary = "파티 생성", description = "맛집 탐험 파티 모집 게시글을 작성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<Void> createParty(@RequestBody PartyCreateRequest partyCreateRequest,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.createParty(partyCreateRequest, userInfo.getId());
        return BaseResponse.okOnlyStatus(HttpStatus.CREATED);
    }

    @Operation(summary = "파티 상세 조회", description = "맛집 탐험 파티 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{partyId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PartyDetailResponse> getPartyDetail(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        PartyDetailResponse response = partyService.getPartyDetail(partyId);
        return BaseResponse.ok(response, HttpStatus.OK);
    }

    @Operation(summary = "파티 목록 조회", description = "파티 상태, 위치, 음식 카테고리로 필터링한 파티 게시글 목록을 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PartyScrollResponse> getParties(
        @RequestParam(required = false, defaultValue = "RECRUITING") PartyStatus status,
        @RequestParam(required = false) GenderCondition gender,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String query,
        @RequestParam(required = false, defaultValue = "0") Long cursor,
        @RequestParam(required = false, defaultValue = "10") int size,
        @AuthenticationPrincipal PrincipalUser userInfo
    ) {
        PartyScrollResponse response = partyService.searchParties(status, gender,
            location, category, query, cursor, size);
        return BaseResponse.ok(response, HttpStatus.OK);
    }

    @Operation(summary = "파티 참여", description = "맛집 탐험 파티를 참여합니다.")
    @PostMapping("/{partyId}/join")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> joinParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.joinParty(partyId, userInfo.getId());
        return BaseResponse.okOnlyStatus(HttpStatus.OK);
    }

    @Operation(summary = "파티 탈퇴", description = "맛집 탐험 파티를 참여를 취소합니다.")
    @PostMapping("/{partyId}/quit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.quitParty(partyId, userInfo.getId());
    }

    @Operation(summary = "파티 삭제", description = "맛집 탐험 파티 게시글을 삭제합니다.")
    @DeleteMapping("/{partyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.deleteParty(partyId);
    }

    @Operation(summary = "파티 모집 완료 상태 변경", description = "모집중인 파티를 모집종료 상태로 변경합니다.")
    @PatchMapping("/{partyId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> completeParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.completePartyRecruit(partyId);
        return BaseResponse.okOnlyStatus(HttpStatus.OK);
    }

}

