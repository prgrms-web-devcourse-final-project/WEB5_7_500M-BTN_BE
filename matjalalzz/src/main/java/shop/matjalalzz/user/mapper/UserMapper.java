package shop.matjalalzz.user.mapper;


import org.springframework.security.crypto.password.PasswordEncoder;
import shop.matjalalzz.user.domain.User;
import shop.matjalalzz.user.dto.Delete;
import shop.matjalalzz.user.dto.DeleteUserRequest;
import shop.matjalalzz.user.dto.SingUpRequest;

public class UserMapper {

	public static User toUser(SingUpRequest singUpRequest, PasswordEncoder passwordEncoder) {
		return User.builder()
			.email(singUpRequest.email())
			.password(passwordEncoder.encode(singUpRequest.password()))  // 암호화
			.nickname(singUpRequest.nickname())
			.phoneNumber(singUpRequest.phoneNumber())
			.age(singUpRequest.age())
			.name(singUpRequest.name())
			.gender(singUpRequest.gender())
			.build();
	}

	public static Delete toDelete(DeleteUserRequest deleteUserRequest) {
		return Delete.builder().email(deleteUserRequest.email())
			.password(deleteUserRequest.password())
			.build();
	}
}
