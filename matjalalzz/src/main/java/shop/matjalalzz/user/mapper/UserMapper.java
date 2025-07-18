package shop.matjalalzz.user.mapper;


import org.springframework.security.crypto.password.PasswordEncoder;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.SignUpRequest;
import shop.matjalalzz.user.entity.User;

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

    public static MyInfoResponse toMyInfoResponse(User user, String profile) {
        return MyInfoResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .name(user.getName())
            .age(user.getAge())
            .gender(user.getGender())
            .point(user.getPoint())
            .phoneNumber(user.getPhoneNumber())
            .profile(profile)
            .build();
    }
}
