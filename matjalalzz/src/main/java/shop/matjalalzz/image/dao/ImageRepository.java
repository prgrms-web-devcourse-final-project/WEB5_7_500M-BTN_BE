package shop.matjalalzz.image.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shop.matjalalzz.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByShopId(long shopId);


}
