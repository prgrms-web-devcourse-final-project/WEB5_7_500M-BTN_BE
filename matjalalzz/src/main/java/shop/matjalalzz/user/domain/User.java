package shop.matjalalzz.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.unit.BaseEntity;
import shop.matjalalzz.user.domain.enums.Gender;
import shop.matjalalzz.user.domain.enums.Role;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 320, nullable = false)
    private String email;

    @Column(length = 254, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String nickname;

    private String name;

    private long age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phoneNumber;

    private long point;

    private boolean deleted;

    private String bucket_id;

    private String profile_image_url;



    @Builder
    public User(String email, String password, String nickname, String phoneNumber, String name, long age, Gender gender, String bucket_id, String profile_image_url) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.deleted = false;
        this.point = 0;
        this.role = Role.USER;
        this.bucket_id = bucket_id;
        this.profile_image_url = profile_image_url;
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
    public void updateProfileImageUrl(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

}