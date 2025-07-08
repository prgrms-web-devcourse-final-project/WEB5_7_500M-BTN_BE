package shop.matjalalzz.domain.party.mock.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.domain.party.mock.entity.MockUser;

public interface MockUserRepository extends JpaRepository<MockUser, Long> {

}
