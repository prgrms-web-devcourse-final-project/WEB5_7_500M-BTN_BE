package shop.matjalalzz.shop.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.shop.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByBusinessCodeOrRoadAddress(long businessCode, String roadAddress);

}