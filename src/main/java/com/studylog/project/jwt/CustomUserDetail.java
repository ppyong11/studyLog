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
    //principal 객체 넣기 전에 findById로 넣어서 토큰 주인 객체로 설정됨
    private final UserEntity user; //이건 Getter가 자동 생성해 줌 (추상 메서드 X)

    public CustomUserDetail(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole() != null && user.getRole() ? "ROLE_ADMIN" : "ROLE_USER")
        );
    }

    //UserDetails의 추상 메서드라서 오버라이드  필요
    @Override
    public String getPassword() {
        return user.getPw();
    }

    @Override
    public String getUsername() {
        return user.getId();
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
