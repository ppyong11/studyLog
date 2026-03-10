package com.studylog.project.fixture;

import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;

public class UserFixture {

    public static UserEntity createAndSaveUser(String suffix, UserRepository repo) {
        UserEntity user = UserEntity.builder()
                .id("test" + suffix)
                .pw("1234567")
                .nickname("테스트유저" + suffix)
                .email("test" + suffix + "@gmail.com")
                .build();

        return repo.save(user); // 저장된 객체 반환
    }
}
