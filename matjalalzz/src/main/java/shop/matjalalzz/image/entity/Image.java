package shop.matjalalzz.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        @Index(name = "image_idx", columnList = "inquiry_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_shop_image_index", columnNames = {"shop_id",
            "image_index"}),
        @UniqueConstraint(name = "unique_review_image_index", columnNames = {"review_id",
            "image_index"}),
        @UniqueConstraint(name = "unique_inquiry_image_index", columnNames = {"inquiry_id",
            "image_index"}),

    }
)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    // s3 경로
    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private long imageIndex;


    @Column(name = "shop_id", nullable = true)
    private Long shopId;


    @Column(name = "review_id", nullable = true)
    private Long reviewId;


    @Column(name = "inquiry_id", nullable = true)
    private Long inquiryId;


}
