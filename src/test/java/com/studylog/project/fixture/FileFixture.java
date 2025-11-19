package com.studylog.project.fixture;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.user.UserEntity;

public class FileFixture {

    public static FileEntity createAndSaveFile(UserEntity user, BoardEntity board, String draftId, FileRepository repo) {
        FileEntity file = FileEntity.builder()
                .user(user)
                .board(board)
                .draft(draftId)
                .size(66756L)
                .path("test/test.jpg")
                .originalName("test.png")
                .name("test.png")
                .type("image/png")
                .build();

        return repo.save(file);
    }
}
