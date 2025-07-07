package shop.matjalalzz.user.dto;

import lombok.Builder;

@Builder
public record Delete(String email, String password) {
}
