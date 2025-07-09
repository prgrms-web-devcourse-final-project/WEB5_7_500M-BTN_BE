package shop.matjalalzz.party.mock.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.party.mock.entity.MockUser;

public interface MockUserRepository extends JpaRepository<MockUser, Long> {

}
