package shop.matjalalzz.shop.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shop.matjalalzz.shop.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByBusinessCodeOrRoadAddress(String businessCode, String roadAddress);

}
