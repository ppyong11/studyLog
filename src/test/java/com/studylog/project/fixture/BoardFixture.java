package com.studylog.project.fixture;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;

public class BoardFixture {
    public static BoardEntity createAndSaveBoard(UserEntity user, CategoryEntity category, BoardRepository repo) {
        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title("테스트 게시글")
                .content("테스트")
                .build();

        return repo.save(board);
    }
}
