package com.studylog.project;

import com.studylog.project.user.UserController;
import com.studylog.project.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebMvcTest(controllers = UserController.class)
class StudyLogApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void id_닉네임_동시_입력시_에러() throws Exception {
        mockMvc.perform(get("/sign-in/check-info")
                .param("id", "test")
                .param("nickname", "테스트"))
        .andExpect(status().isBadRequest());
    }

    @Test
    void 아무_값도_없는_경우_에러() throws Exception {
        mockMvc.perform(get("/sign-in/check-info"))
                .andExpect(status().isBadRequest());
    }
}
