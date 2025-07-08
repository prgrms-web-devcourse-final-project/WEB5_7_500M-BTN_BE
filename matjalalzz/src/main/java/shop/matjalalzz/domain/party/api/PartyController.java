package shop.matjalalzz.domain.party.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.domain.party.app.PartyService;
import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.domain.party.dto.PartyScrollResponse;
import shop.matjalalzz.domain.party.entity.GenderCondition;
import shop.matjalalzz.domain.party.entity.PartyStatus;
import shop.matjalalzz.global.unit.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("parties")
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    public BaseResponse<Void> createParty(@RequestBody PartyCreateRequest partyCreateRequest) {
        partyService.createParty(partyCreateRequest);
        return BaseResponse.okOnlyStatus(HttpStatus.CREATED);
    }

    @GetMapping("/{partyId}")
    public BaseResponse<PartyDetailResponse> getPartyDetail(@PathVariable Long partyId) {
        PartyDetailResponse response = partyService.getPartyDetail(partyId);
        return BaseResponse.ok(response, HttpStatus.OK);
    }

    @GetMapping
    public BaseResponse<PartyScrollResponse> getParties(
        @RequestParam(required = false, defaultValue = "RECRUITING") PartyStatus status,
        @RequestParam(required = false) GenderCondition gender,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String query,
        @RequestParam(required = false, defaultValue = "0") Long cursor,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        partyService.searchParties(status, gender, location, category, query, cursor, size);
    }

    @PostMapping("/{partyId}/join")
    public BaseResponse<Void> joinParty(@PathVariable Long partyId) {
        partyService.joinParty(partyId);
        return BaseResponse.okOnlyStatus(HttpStatus.OK);
    }


}

