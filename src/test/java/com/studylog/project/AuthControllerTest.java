package com.studylog.project;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.SuccessResponse;
import com.studylog.project.jwt.AuthController;
import com.studylog.project.jwt.AuthService;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static com.mysema.commons.lang.Assert.assertThat;
import static jdk.dynalink.linker.support.Guards.isNull;
import static net.bytebuddy.matcher.ElementMatchers.any;

import static org.springframework.http.RequestEntity.post;
import static org.springframework.http.ResponseEntity.status;
import static reactor.core.publisher.Mono.when;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 💡 컨트롤러 테스트에서는 이제 authService 하나만 모킹하면 됩니다!
    // 레디스, UserService, SecurityFilterChain 등은 컨트롤러에서 직접 안 쓰면 지워도 무방합니다.
    @MockBean
    private AuthService authService;

    @Test
    void 토큰_재발급_실패() throws Exception {
        // 1. 가짜 AuthService가 예외를 던지도록 '미리 세팅'합니다. (쿠키가 없을 때)
        when(authService.createNewToken(any()))
                .thenThrow(new CustomException(ErrorCode.AUTH_REQUIRED));

        // 2. 쿠키 없이 API를 찌릅니다.
        mockMvc.perform(post("/api/refresh"))
                .andExpect(result ->
                        assertThat(resultgetResolvedException())
                                .isInstanceOf(CustomException.class)
                );
    }

    @Test
    void 토큰_재발급_성공() throws Exception {
        // 1. 가짜 AuthService가 정상적인 응답을 하도록 '미리 세팅'합니다.
        when(authService.refreshAccessToken(anyString(), any()))
                .thenReturn(ResponseEntity.ok(SuccessResponse.of("로그인이 연장되었습니다.")));

        // 2. 정상적인 쿠키를 담아 API를 찌릅니다.
        mockMvc.perform(post("/api/refresh")
                        .cookie(new Cookie("refresh_token", "rtToken")))
                .andExpect(status(200));
    }
}