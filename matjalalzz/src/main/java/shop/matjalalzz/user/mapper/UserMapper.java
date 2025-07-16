package shop.matjalalzz.user.mapper;


import java.util.function.Consumer;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
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

    public static MyInfoResponse toMyInfoResponse(User user, String baseUrl) {
        return MyInfoResponse.builder()
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .name(user.getName())
            .age(user.getAge())
            .gender(user.getGender())
            .point(user.getPoint())
            .phoneNumber(user.getPhoneNumber())
            .profile(user.getProfileKey() == null ? null : baseUrl + user.getProfileKey())
            .build();
    }

    public static void update(User user, OAuthSignUpRequest dto) {
        user.updateNickname(request.nickname());
        user.updatePhoneNumber(request.phoneNumber());
        user.updateName(request.name());
        user.updateAge(request.age());
        user.updateGender(request.gender());
    }

    public static void update(User user, MyInfoUpdateRequest dto) {
        applyIfNotNull(dto.nickname(), user::updateNickname);
        applyIfNotNull(dto.age(), user::updateAge);
        applyIfNotNull(dto.phoneNumber(), user::updatePhoneNumber);
        applyIfNotNull(dto.profileKey(), user::updateProfileKey);
    }

    private static <T> void applyIfNotNull(T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
