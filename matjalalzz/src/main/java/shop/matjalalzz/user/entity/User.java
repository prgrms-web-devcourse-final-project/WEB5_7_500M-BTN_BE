package shop.matjalalzz.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

@Entity
@Getter
@Table(name = "`user`")
@SQLRestriction("deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String oauthId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String name;

    private int age;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    private int point = 0;

    @Size(max = 255)
    private String profileKey;

    @Builder
    public User(String email, String oauthId, String password, String nickname, String phoneNumber,
        String name, int age, Gender gender, String profileKey) {
        this.email = email;
        this.oauthId = oauthId;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.profileKey = profileKey;
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
    public void updateAge(int age) {
        this.age = age;
    }

    // point 변경 메서드
    public void updatePoint(int point) {
        this.point += point;
    }

    //삭제 여부 변경 메서드
    public void updateDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    //사진 변경 메서드
    public void updateProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    // 권한 변경 메서드
    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }
}