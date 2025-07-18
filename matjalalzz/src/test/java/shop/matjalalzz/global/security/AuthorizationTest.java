package shop.matjalalzz.global.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import shop.matjalalzz.user.entity.enums.Role;
import shop.matjalalzz.util.WithCustomMockUser;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("anonymous 접근 제한 테스트")
    void anonymousAccessTest() throws Exception {
        mvc.perform(get("/test"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("user 접근 제한 테스트")
    @WithCustomMockUser(role = Role.USER)
    void userAccessTest() throws Exception {
        mvc.perform(get("/test"))
            .andExpect(content().string("user"));

        mvc.perform(get("/owner/test"))
            .andExpect(status().isForbidden());

        mvc.perform(get("/admin/test"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("owner 접근 제한 테스트")
    @WithCustomMockUser(role = Role.OWNER)
    void ownerAccessTest() throws Exception {
        mvc.perform(get("/owner/test"))
            .andExpect(content().string("owner"));

        mvc.perform(get("/admin/test"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin 접근 제한 테스트")
    @WithCustomMockUser(role = Role.ADMIN)
    void adminAccessTest() throws Exception {
        mvc.perform(get("/admin/test"))
            .andExpect(content().string("admin"));
    }
}
