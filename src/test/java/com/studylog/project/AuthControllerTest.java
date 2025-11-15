package com.studylog.project;

import com.studylog.project.global.response.SuccessResponse;
import com.studylog.project.jwt.AuthController;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.jwt.JwtService;
import com.studylog.project.jwt.JwtToken;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse response;

    @Test
    void 토큰_재발급_성공() {
        // given
        CustomUserDetail user = mock(CustomUserDetail.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("RT:test-refresh")).thenReturn("1");
        when(jwtService.createNewToken(any(), any(), any()))
                .thenReturn(new JwtToken("Bearer", "newAccess", "newRefresh"));
        when(jwtService.createCookie(any(), any(), any(), anyInt()))
                .thenReturn("cookie-string");

        // when
        ResponseEntity<SuccessResponse<Void>> result = authController.refreshAccessToken(
                "test-access", "test-refresh", user, response
        );

        // then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("로그인이 연장되었습니다.", result.getBody().message());
    }
}

