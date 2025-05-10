package com.studylog.project.service;

import com.studylog.project.entity.UserEntity;
import com.studylog.project.repository.UserRepository;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity saveUser(UserEntity user) {
        validateDuplicateMemeber(user);
        return userRepository.save(user);
    }
    public void validateDuplicateMemeber(UserEntity user) {
        UserEntity findEmail = userRepository.findByEmail(user.getEmail());
        if (findEmail != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public Boolean existsId(String id) {
        return userRepository.existsById(id);
    }
    public Boolean existsNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
