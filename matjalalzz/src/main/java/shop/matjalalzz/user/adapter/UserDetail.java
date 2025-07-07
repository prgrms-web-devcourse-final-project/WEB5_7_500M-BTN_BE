package shop.matjalalzz.user.adapter;


import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import shop.matjalalzz.user.domain.enums.Role;
import shop.matjalalzz.user.domain.User;


@Slf4j
@Accessors(chain = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//구현 필수 메서드
public class UserDetail implements UserDetails {

	private long id;
	private String email;
	private String password;
	private Role role;

	@Builder
	public UserDetail(String password, String email, Role role) {
		this.password = password;
		this.email = email;
		this.role = role;
	}

	public static UserDetail UserDetailsMake(User findUser) {
		UserDetail userDetail = new UserDetail();
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

}

