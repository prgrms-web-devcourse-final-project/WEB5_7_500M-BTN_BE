package shop.matjalalzz.user.domain;// package io.security_JWT.backend.user.domain;
//
// import jakarta.persistence.*;
// import lombok.*;
//
// import java.util.Date;
//
// //  "정상적으로 만료된 게 아니라, 강제로 로그아웃된 토큰" 을 저장하는 용도로 블랙리스트 사용
// @Entity
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// public class BlackList {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//
//     @Column(nullable = false, unique = true, length = 500)
//     private String inversionAccessToken;
//
//     // 언제까지 블랙리스트에 유지할지 (accessToken의 만료 시각)
//     @Column(nullable = false)
//     private Date expiration;
//
//     //누가 어떤 사유로 로그아웃했는지, 특정 유저가 자주 로그아웃하는지 등의 사용자 기반의 통계나 감시를 해야 한다면 User 과 연관관계를 맺을 수 있지만
//     //단순히 Access token이 만료되거나 무효화 상태로 블랙리스트에 넣은 토큰인지 아닌지만 확인하는 지금 같은 경우엔 필요가 없다.
//
//     //    [문제 상황]
//     //    1. A 사용자가 로그인해서 JWT 발급 받음
//     //    2. A가 로그아웃함 (토큰을 프론트에서 삭제함)
//     //    3. 그런데 그 토큰을 누군가 복사해서 계속 쓰고 있음 -> 보안 문제 발생
//     //
//     //    [해결]
//     //    로그아웃 시 해당 Access Token을 DB에 "블랙리스트"로 저장
//     //    → 이후 요청 시 이 토큰이 블랙리스트에 있는지 먼저 검사 → 있으면 강제 거절
//
//     @Builder
//     public BlackList(String inversionAccessToken, Date expiration) {
//         this.inversionAccessToken = inversionAccessToken;
//         this.expiration = expiration;
//     }
//
// }
