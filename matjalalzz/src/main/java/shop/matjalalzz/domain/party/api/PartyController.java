package shop.matjalalzz.domain.party.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.domain.party.app.PartyService;
import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.global.unit.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("parties")
public class PartyController {

	private final PartyService partyService;

	@PostMapping
	public BaseResponse<Void> createParty(@RequestBody PartyCreateRequest partyCreateRequest){
		partyService.createParty(partyCreateRequest);
		return BaseResponse.okOnlyStatus(HttpStatus.CREATED);
	}

	@GetMapping("/{partyId}")
	public BaseResponse<PartyDetailResponse> getPartyDetail(@PathVariable Long partyId){
		partyService.getPartyDetail(partyId);
	}

}

