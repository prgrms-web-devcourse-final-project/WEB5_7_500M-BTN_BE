package shop.matjalalzz.user.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//권한을 가진 admin만 통과가 되는지 테스트
@RestController
public class TestController {

    @GetMapping("/admin/test")
    public String adminTest() {
        return "admin";
    }

    @GetMapping("/owner/test")
    public String ownerTest() {
        return "owner";
    }

    @GetMapping("/test")
    public String userTest() {
        return "user";
    }

}