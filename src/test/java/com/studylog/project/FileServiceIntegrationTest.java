package com.studylog.project;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.fixture.BoardFixture;
import com.studylog.project.fixture.CategoryFixture;
import com.studylog.project.fixture.FileFixture;
import com.studylog.project.fixture.UserFixture;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@Transactional
public class FileServiceIntegrationTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired // user 저장하기 위해 사용
    private UserRepository userRepository;

    @Autowired // board에 필요한 객체
    private CategoryRepository categoryRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Test
    void 임시_값_미일치로_파일_삭제_실패() {
        UserEntity user = UserFixture.createAndSaveUser("3", userRepository);
        FileEntity file = FileFixture.createAndSaveFile(user, null, "test-123", fileRepository);

        assertThatCode(() -> fileService.deleteTempMeta(file.getId(), "test-121", user))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저_미일치로_파일_삭제_실패() {
        UserEntity user1 = UserFixture.createAndSaveUser("3", userRepository);
        UserEntity user2 = UserFixture.createAndSaveUser("4", userRepository);

        FileEntity file = FileFixture.createAndSaveFile(user1, null, "test-123", fileRepository);

        assertThatCode(() -> fileService.deleteTempMeta(file.getId(), "test-123", user2))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 게시글_파일_매핑_성공() {
        UserEntity user1 = UserFixture.createAndSaveUser("3", userRepository);

        FileEntity file1 = FileFixture.createAndSaveFile(user1, null, "test-123",fileRepository);
        FileEntity file2 = FileFixture.createAndSaveFile(user1, null, "test-123",fileRepository);

        CategoryEntity category = CategoryFixture.createAndSaveCategory(user1, categoryRepository);
        BoardEntity board = BoardFixture.createAndSaveBoard(user1, category, boardRepository);

        fileService.attachDraftFilesToBoard(user1, board, "test-123");

        // 매핑 성공
        assertThat(file1.getBoard()).isEqualTo(board);
        assertThat(file2.getBoard()).isEqualTo(board);

        assertThat(file1.getDraftId()).isEqualTo(null);
    }

    @Test
    void 게시글이_매핑된_파일_삭제() {
        UserEntity user1 = UserFixture.createAndSaveUser("3", userRepository);
        CategoryEntity category = CategoryFixture.createAndSaveCategory(user1, categoryRepository);
        BoardEntity board = BoardFixture.createAndSaveBoard(user1, category, boardRepository);

        FileEntity file1 = FileFixture.createAndSaveFile(user1, board, null, fileRepository);

        // 삭제 전
        Optional<FileEntity> deleted = fileRepository.findById(file1.getId());
        assertThat(deleted).isNotEmpty();

        fileService.deleteMeta(file1.getId(), board.getId(), user1);

        // 삭제 후
        deleted = fileRepository.findById(file1.getId());
        assertThat(deleted).isEmpty();
    }
}
