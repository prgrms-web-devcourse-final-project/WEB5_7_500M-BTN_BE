package shop.matjalalzz.user.dao;

import java.util.List;
import java.util.Map;

public interface CustomUserRepository {

    Map<Long, String> getUsersNickname(List<Long> ids);

}
