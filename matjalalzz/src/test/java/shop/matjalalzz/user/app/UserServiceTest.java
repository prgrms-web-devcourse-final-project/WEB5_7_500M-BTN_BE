package shop.matjalalzz.user.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;

    @Test
    @DisplayName("내 정보 조회 테스트")
    void getMyInfoTest() {
        // given
        User user = User.builder()
            .email("minji97@gmail.com")
            .nickname("맛잘알민지")
            .age(28)
            .gender(Gender.W)
            .phoneNumber("010-1234-5678")
            .profileKey("/profile/1/img.png")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        MyInfoResponse resp = userService.getMyInfo(1L);

        // then
        assertThat(resp.email()).isEqualTo("minji97@gmail.com");
        assertThat(resp.profile()).contains(user.getProfileKey());
    }

    @Test
    @DisplayName("내 정보 수정 테스트")
    void updateMyInfoTest() {
        // given
        User user = User.builder()
            .nickname("old")
            .age(20)
            .phoneNumber("010-1111-1111")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MyInfoUpdateRequest dto = MyInfoUpdateRequest.builder()
            .nickname("newNick")
            .age(25)
            .build();

        // when
        userService.updateMyInfo(1L, dto);

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getAge()).isEqualTo(25);
        assertThat(user.getPhoneNumber())
            .isEqualTo("010-1111-1111");
    }
}
