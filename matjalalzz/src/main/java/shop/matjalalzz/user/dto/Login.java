package shop.matjalalzz.user.dto;


// 클라이언트에게 반환하지 않고 조합 용도로만 사용중
public record Login(String accessToken, String refreshToken) {

}
