package shop.matjalalzz.user.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Gender;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PreSignedProvider preSignedProvider;

    @Test
    @DisplayName("내 정보 조회 테스트")
    void getMyInfoTest() {
        // given
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("minji97@gmail.com");
        when(user.getGender()).thenReturn(Gender.W);
        when(user.getProfileKey()).thenReturn("/profile/1/img.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        MyInfoResponse response = userService.getMyInfo(1L);

        // then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.gender()).isEqualTo(Gender.W);
        assertThat(response.email()).isEqualTo("minji97@gmail.com");
        assertThat(response.profile()).contains(user.getProfileKey());
    }

    @Test
    @DisplayName("내 정보 수정 테스트")
    void updateMyInfoTest() {
        // given
        User user = User.builder()
            .nickname("old")
            .age(20)
            .phoneNumber("010-1111-1111")
            .profileKey("/profile/1/1234_img.png")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MyInfoUpdateRequest request = MyInfoUpdateRequest.builder()
            .nickname("newNick")
            .age(25)
            .phoneNumber("010-2222-2222")
            .profileKey("/profile/1/5678_img.png")
            .build();

        // when
        userService.updateMyInfo(1L, request);

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getAge()).isEqualTo(25);
        assertThat(user.getPhoneNumber()).isEqualTo("010-2222-2222");
        assertThat(user.getProfileKey()).isEqualTo("/profile/1/5678_img.png");
    }
}
