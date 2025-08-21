package shop.matjalalzz.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.SignUpRequest;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(SignUpRequest signUpRequest, String password, Role role) {
        return User.builder()
            .role(role)
            .email(signUpRequest.email())
            .password(password)
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
