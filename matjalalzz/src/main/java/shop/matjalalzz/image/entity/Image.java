package shop.matjalalzz.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(
    indexes = {
        @Index(name = "image_idx", columnList = "shop_id"),
        @Index(name = "image_idx", columnList = "review_id"),
        @Index(name = "image_idx", columnList = "user_id")
    }
)
//@Check(constraints = "(shop_id IS NULL) <> (review_id IS NULL)")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id")
    private Long id;

    // s3 경로
    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private long imageIndex;


    @Column(name = "shop_id",  nullable = true)
    private Long shopId;


    @Column(name = "review_id",  nullable = true)
    private Long reviewId;

    // 프로필 사진으로 일단 기본 사진이 없으니 true로 합니다
    @Column(name = "profile_id",  nullable = true)
    private Long userId;

    //이미지 저장 성공 여부
    private boolean completed = false;



}
