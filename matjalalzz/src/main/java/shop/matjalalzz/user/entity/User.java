package shop.matjalalzz.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.user.entity.enums.Gender;
import shop.matjalalzz.user.entity.enums.Role;

@Entity
@Getter
@Table(name = "`user`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oauthId;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 20)
    private String phoneNumber;

    private int point = 0;

    @Size(max = 255)
    private String profileKey;

    @Version
    private long version;

    @Builder
    public User(String email, String oauthId, Role role, String password, String nickname, String phoneNumber,
        String name, Integer age, Gender gender, String profileKey) {
        this.email = email;
        this.oauthId = oauthId;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.profileKey = profileKey;
    }

    // point 변경 메서드
    public void increasePoint(int point) {
        this.point += point;
    }

    public void decreasePoint(int point) {
        this.point -= point;
    }

    // 권한 변경 메서드
    public void updateRole(Role role) {
        this.role = role;
    }

    public void oauthSignup(OAuthSignUpRequest dto) {
        this.nickname = dto.nickname();
        this.phoneNumber = dto.phoneNumber();
        this.name = dto.name();
        this.age = dto.age();
        this.gender = dto.gender();
        this.role = Role.USER;
    }

    public void update(MyInfoUpdateRequest dto) {
        this.nickname = dto.nickname();
        this.age = dto.age();
        this.phoneNumber = dto.phoneNumber();
        this.profileKey = dto.profileKey();
    }
}