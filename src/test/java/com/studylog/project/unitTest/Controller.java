package com.studylog.project.unitTest;

import com.studylog.project.jwt.JwtTokenProvider;
import com.studylog.project.mail.MailService;
import com.studylog.project.user.UserController;
import com.studylog.project.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser
public class Controller {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private MailService mailService;


    @Test
    void id_닉네임_동시_입력시_에러() throws Exception {
        mockMvc.perform(get("/study-log/sign-in/check-info")
                        .param("id", "test")
                        .param("nickname", "테스트"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 아무_값도_없는_경우_에러() throws Exception {
        mockMvc.perform(get("/study-log/sign-in/check-info"))
                .andExpect(status().isBadRequest());
    }
}
