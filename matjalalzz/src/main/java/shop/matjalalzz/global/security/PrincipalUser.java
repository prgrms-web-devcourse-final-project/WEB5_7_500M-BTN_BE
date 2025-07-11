package shop.matjalalzz.global.security;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//구현 필수 메서드
public class PrincipalUser implements UserDetails, OAuth2User {

	private long id;
	private String email;
	private String password;
	private Role role;
	private Map<String, Object> attributes;

	@Builder
	public PrincipalUser(long id, String password, String email, Role role) {
		this.id = id;
		this.password = password;
		this.email = email;
		this.role = role;
	}

	public static PrincipalUser UserDetailsMake(User findUser) {
		PrincipalUser userDetail = new PrincipalUser();
		userDetail.id = findUser.getId();
		userDetail.email = findUser.getEmail();
		userDetail.password = findUser.getPassword();
		userDetail.role = findUser.getRole();
		return userDetail;
	}

	//인가 검사 시 사용하는 것, 로그인한 사용자(Principal)의 권한(Role)을 설정하는 메서드
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public String getName() {
		return this.email;
	}

	public Long getUserId() { return this.id;}
}

