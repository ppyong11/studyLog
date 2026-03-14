package com.studylog.project.jwt;

import com.studylog.project.user.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetail implements UserDetails {
    private final UserEntity user;
    private final Long userId;
    private final String loginId;
    private final boolean isAdmin;

    // DB 조회 후 실제 엔티티 넣을 때
    public CustomUserDetail(UserEntity user) {
        this.user = user;
        this.userId = user.getUser_id();
        this.loginId = user.getId();
        this.isAdmin = user.getRole() != null && user.getRole();
    }

    // JWT 인증 시 (가짜 객체)
    public CustomUserDetail(Long userId, String id, String role) {
        this.user = null;
        this.userId = userId;
        this.loginId = id;
        this.isAdmin = !role.equals("ROLE_USER");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole() != null && user.getRole() ? "ROLE_ADMIN" : "ROLE_USER")
        );
    }

    //UserDetails의 추상 메서드라서 오버라이드 필요
    @Override
    public String getPassword() {
        return user != null ? user.getPw() : null;
    }

    @Override
    public String getUsername() {
        return loginId; // user 대신 직접 필드 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
