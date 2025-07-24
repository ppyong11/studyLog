package com.studylog.project.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylog.project.global.filterExceptionHandler.CustomAuthenticationEntryPoint;
import com.studylog.project.jwt.JwtAuthenticationFilter;
import com.studylog.project.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //Config 파일로 설정,이거 있어야 @Bean 스캔 가능
@EnableWebSecurity //WebSecurity 활성화
public class SecurityConfig {
    @Bean //스프링 컨테이너에 bean 등록 & 주입된 것들을 싱글톤으로 관리하겠다
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                //RestAPI이므로 basic auth, csrf 보안 사용 X
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                //JWT 사용하니까 세션 사용 X
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            //아래 API 요청 모두 허가
                            "/api/sign-in/**",
                            "/api/log-in",
                            "/api/", //뒷 엔드포인트도 다 로긘 처리
                            "/api", //메인
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/webjars/**"
                ).permitAll()
                //그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean //or 클래스에 @Component 붙이기
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    //각 파라미터에 @Component 등 어노테이션이 붙어있으면 알아서 빈 찾아서 파라미터에 주입해줌, 그리고 이 메서드도 저 위 파라미터에 주입하는 것
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
