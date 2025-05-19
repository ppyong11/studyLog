package com.studylog.project.jwt;

import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity= userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("[%s]에 해당하는 회원을 찾을 수 없습니다.", username)));
        return new CustomUserDetail(userEntity); //userEntity 객체가 Custom~ 생성자로 둘어감
    }
}
