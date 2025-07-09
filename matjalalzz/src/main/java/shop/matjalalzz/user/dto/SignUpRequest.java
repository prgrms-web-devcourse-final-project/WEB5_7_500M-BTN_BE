package shop.matjalalzz.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import shop.matjalalzz.user.entity.enums.Gender;

public record SignUpRequest(
	@Email(message = "잘못된 이메일 형식입니다.")
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	String email,

	@NotBlank(message = "비밀번호 입력은 필수 입력값입니다.")
	String password,

	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	String nickname,

	@NotBlank(message = "전화번호는 필수 입력값입니다.")
	String phoneNumber,

	@NotBlank(message = "이름은 필수 입력값입니다.")
	String name,

	@Min(value = 0, message = "나이는 0 이상이어야 합니다.")
	long age,

	@NotNull(message = "성별은 필수 입력값입니다.")
	Gender gender
) {}
