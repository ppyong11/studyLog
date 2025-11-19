package com.studylog.project;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.jwt.AuthController;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.jwt.JwtService;
import com.studylog.project.jwt.JwtToken;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserService;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private UserService userService;

    @Test
    void 토큰_재발급_실패() throws Exception {
        UserEntity userEntity = UserEntity.builder()
                .id("test1")
                .pw("1234567")
                .nickname("테스트")
                .email("test@test.com")
                .build();

        CustomUserDetail customUserDetail = new CustomUserDetail(userEntity);

        mockMvc.perform(post("/api/refresh")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(customUserDetail, null, customUserDetail.getAuthorities())
                        ))
                        .cookie(new Cookie("access_token", "abc")))
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(CustomException.class)
            );
    }

    @Test
    void 토큰_재발급_성공() throws Exception {
        UserEntity userEntity = UserEntity.builder()
                .id("test1")
                .pw("1234567")
                .nickname("테스트")
                .email("test@test.com")
                .build();

        CustomUserDetail customUserDetail = new CustomUserDetail(userEntity);

        // userId 설정
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("RT:rtToken")).thenReturn("test1");

        JwtToken fakeToken = new JwtToken("Bearer", "newAccessToken", "newRefreshToken");
        when(jwtService.createNewToken(any(), any(), any())).thenReturn(fakeToken);

        when(jwtService.createCookie(any(), any(), any(), anyInt()))
                .thenReturn("cookie-string");

        mockMvc.perform(post("/api/refresh")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(customUserDetail, null, customUserDetail.getAuthorities())
                        ))
                        .cookie(new Cookie("access_token", "abc"))
                        .cookie(new Cookie("refresh_token", "rtToken")))
                .andExpect(status().isOk());
    }
}

