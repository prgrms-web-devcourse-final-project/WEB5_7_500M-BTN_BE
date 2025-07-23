package shop.matjalalzz.party.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.dto.KickoutResponse;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.dto.PartySearchParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("parties")
@Tag(name = "파티 API", description = "맛집 탐험 파티 관련 API")
public class PartyController {

    private final PartyService partyService;

    @Operation(summary = "파티 생성", description = "맛집 탐험 파티 모집 게시글을 작성합니다.(Completed)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<Void> createParty(@Valid @RequestBody PartyCreateRequest partyCreateRequest,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.createParty(partyCreateRequest, userInfo.getId());
        return BaseResponse.ok(BaseStatus.CREATED);
    }

    @Operation(summary = "파티 상세 조회", description = "맛집 탐험 파티 게시글 상세 정보를 조회합니다.(Completed)")
    @GetMapping("/{partyId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PartyDetailResponse> getPartyDetail(@PathVariable Long partyId) {
        PartyDetailResponse response = partyService.getPartyDetail(partyId);
        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(summary = "파티 목록 조회", description = """
        파티 상태, 위치, 음식 카테고리로 필터링한 파티 게시글 목록을 조회합니다. (Completed)
        
        예시 /parties?status=RECRUITING&minAge=10&categories=CHICKEN&categories=JAPANESE
        
        | 필드 명     | 자료형    | 필수 여부  | 설명                                            | 기본값                   |
        |------------|---------|-----------|-------------------------------------------------|------------------------|
        | status     | string  | Optional  | 파티 상태                                        | 전체                    |
        | gender     | string  | Optional  | 모집 성별 조건(A일 시 성별 무관인 파티만 조회합니다.)   |  전체                   |
        | minAge     | int     | Optional  | 모집 최소 나이                                    | 전체                    |
        | maxAge     | int     | Optional  | 모집 최대 나이                                    | 전체                    |
        | location   | string  | Optional  | 시/도 단위 파티 위치                               | 전체                    |
        | category   | string  | Optional  | 음식 카테고리 (다중 선택 가능)                       | 전체                    |
        | query      | string  | Optional  | 파티 제목 검색 키워드                               | 전체                    |
        | cursor       | int  | Optional   | 페이징 마지막 파티 id                               | 첫번째 페이지            |
        """)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PartyScrollResponse> getParties(
        @ParameterObject PartySearchParam param,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PartyScrollResponse response = partyService.searchParties(param, size);
        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(summary = "파티 참여", description = "맛집 탐험 파티를 참여합니다.(Completed)")
    @PostMapping("/{partyId}/join")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> joinParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.joinParty(partyId, userInfo.getId());
        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(summary = "파티 탈퇴", description = "맛집 탐험 파티를 참여를 취소합니다.(Completed)")
    @PostMapping("/{partyId}/quit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.quitParty(partyId, userInfo.getId());
    }

    @Operation(summary = "파티 강퇴", description = "맛집 탐험 파티에서 파티원을 강제 퇴장시킵니다.(Completed)")
    @PostMapping("/{partyId}/kick/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<KickoutResponse> quitParty(@PathVariable Long partyId,
        @PathVariable Long userId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        KickoutResponse response = partyService.kickout(partyId, userInfo.getId(), userId);
        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(summary = "파티 삭제", description = "맛집 탐험 파티 게시글을 삭제합니다.(Completed)")
    @DeleteMapping("/{partyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.deleteParty(partyId, userInfo.getId());
    }

    @Operation(summary = "파티 모집 완료 상태 변경", description = "모집중인 파티를 모집종료 상태로 변경합니다.(Completed)")
    @PatchMapping("/{partyId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> completeParty(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.completePartyRecruit(partyId, userInfo.getId());
        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(summary = "파티 예약금 지불", description = "맛집 탐험 파티의 예약을 위한 예약금을 지불합니다.(Completed)")
    @PostMapping("/{partyId}/pay")
    @ResponseStatus(HttpStatus.OK)
    public void payPartyFee(@PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser userInfo) {
        partyService.payReservationFee(partyId, userInfo.getId());
    }
}

