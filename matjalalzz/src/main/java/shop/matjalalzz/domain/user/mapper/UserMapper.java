package shop.matjalalzz.domain.user.mapper;


import org.springframework.security.crypto.password.PasswordEncoder;
import shop.matjalalzz.domain.user.entity.User;
import shop.matjalalzz.domain.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.domain.user.dto.SignUpRequest;

public class UserMapper {

	public static User toUser(SignUpRequest signUpRequest, PasswordEncoder passwordEncoder) {
		return User.builder()
			.email(signUpRequest.email())
			.password(passwordEncoder.encode(signUpRequest.password()))  // 암호화
			.nickname(signUpRequest.nickname())
			.phoneNumber(signUpRequest.phoneNumber())
			.age(signUpRequest.age())
			.name(signUpRequest.name())
			.gender(signUpRequest.gender())
			.build();
	}

	public static User toOAuthUser(OAuthSignUpRequest signUpRequest) {
		return User.builder()
				.nickname(signUpRequest.nickname())
				.phoneNumber(signUpRequest.phoneNumber())
				.age(signUpRequest.age())
				.name(signUpRequest.name())
				.gender(signUpRequest.gender())
				.build();
	}
}
