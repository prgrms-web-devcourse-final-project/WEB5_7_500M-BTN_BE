package shop.matjalalzz.domain.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.unit.BaseEntity;
import shop.matjalalzz.domain.user.domain.enums.Gender;
import shop.matjalalzz.domain.user.domain.enums.Role;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @Column(unique = true)
    private String oauthId;

    @NotNull
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @NotNull
    @Size(max = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @NotNull
    @Size(max = 20)
    private String nickname;

    @NotNull
    @Size(max = 20)
    private String name;

    private long age;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Size(max = 20)
    private String phoneNumber;

    private long point = 0;

    private boolean deleted = false;

    @Size(max = 255)
    @Column(name = "bucket_id")
    private String bucketId;

    @Size(max = 255)
    @Column(name = "profile_image_url")
    private String profileImageUrl;


    @Builder
    public User(String email, String oauthId, String password, String nickname, String phoneNumber, String name, long age, Gender gender, String bucketId, String profileImageUrl) {
        this.email = email;
        this.oauthId = oauthId;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bucketId = bucketId;
        this.profileImageUrl = profileImageUrl;
    }

    // 닉네임 변경 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 핸드폰번호 변경 메서드
    public void updatePhoneNumber(String phone_number) {
        this.phoneNumber = phone_number;
    }

    //나이 변경 메서드
    public void updateAge(long age) { this.age = age;}

    // point 변경 메서드
    public void updatePoint(Long point) {
        this.point = point;
    }

    //삭제 여부 변경 메서드
    public void updateDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    //사진 변경 메서드
    public void updateProfileImage(String bucketId, String profileImageUrl) {
        this.bucketId = bucketId;
        this.profileImageUrl = profileImageUrl;
    }

    // 권한 변경 메서드
    public void updateRole(Role role) {
        this.role = role;
    }
}