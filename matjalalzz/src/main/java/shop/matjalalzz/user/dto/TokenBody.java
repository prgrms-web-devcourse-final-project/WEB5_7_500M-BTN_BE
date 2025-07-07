package shop.matjalalzz.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.user.domain.enums.Role;


@Getter
@RequiredArgsConstructor
public class TokenBody {
	private Long userId;
	private String email;
	private Role role;

	public TokenBody(Long userId, String email, Role role) {
		this.userId = userId;
		this.email = email;
		this.role = role;
	}
}
