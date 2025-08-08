package shop.matjalalzz.image.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.s3Key from Image i where i.shopId = :shopId order by i.imageIndex asc")
    String findFirstByShopIdOrderByImageIndexAsc(@Param("shopId") Long shopId);


    List<Image> findByShopIdOrderByImageIndexAsc(long shopId);

    @Query("select i.s3Key from Image i where i.shopId =:shopId")
    List<String> findByShopImage(@Param("shopId") long shopId);

    @Query("select i.s3Key from Image i where i.inquiryId =:inquiryId")
    List<String> findByInquiryImage(@Param("inquiryId") long inquiryId);

    List<Image> findByShopId(Long id);

    Optional<Image> findFirstByShopId(Long id);

    void deleteByS3Key(String s3Key);

    Optional<Image> findByShopIdAndImageIndex(Long shopId, long imageIndex);


}
