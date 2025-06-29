package com.studylog.project.unitTest;

import com.studylog.project.jwt.JwtTokenProvider;
import com.studylog.project.mail.MailService;
import com.studylog.project.user.UserController;
import com.studylog.project.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
public class Controller {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private MailService mailService;


    @Test
    void 메일_보내기() throws Exception {
        when(userService.existsEmail(anyString())).thenReturn(false);
        doNothing().when(mailService).sendEmailCode(anyString());
        mockMvc.perform(
                post("/study-log/sign-in/send-email-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"mncxbv1120@gmail.com\",\"code\":\"\"}")
        )
                .andExpect(status().isOk());
    }
/*
    @Test
    void 메일_검증() throws Exception {
        mockMvc.perform(
                post("/study-log/sign-in/verify-email-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mncxbv1120@gmail.com\",\"code\":\"hello world\"}")
                )
                .andExpect(status().isBadRequest());
    }*/
}
