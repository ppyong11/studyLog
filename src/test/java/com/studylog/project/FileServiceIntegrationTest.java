package com.studylog.project;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
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


    private UserEntity createAndSaveUser(String suffix) {
        UserEntity user = UserEntity.builder()
                .id("test" + suffix)
                .pw("1234567")
                .nickname("테스트유저" + suffix)
                .email("test" + suffix + "@gmail.com")
                .build();

        return userRepository.save(user); // 저장된 객체 반환
    }

    private FileEntity createAndSaveFile(UserEntity user, BoardEntity board, String draftId) {
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

        return fileRepository.save(file);
    }

    private BoardEntity createAndSaveBoard(UserEntity user) {
        CategoryEntity category = CategoryEntity.builder()
                .user_id(user)
                .name("테스트")
                .bgColor("#F7F7F7")
                .textColor("#484848")
                .build();

        categoryRepository.save(category);

        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title("테스트 게시글")
                .content("테스트")
                .build();

        return boardRepository.save(board);
    }

    @Test
    void 임시_값_미일치로_파일_삭제_실패() {
        UserEntity user = createAndSaveUser("3");
        FileEntity file = createAndSaveFile(user, null, "test-123");

        assertThatCode(() -> fileService.deleteTempMeta(file.getId(), "test-121", user))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유저_미일치로_파일_삭제_실패() {
        UserEntity user1 = createAndSaveUser("3");
        UserEntity user2 = createAndSaveUser("4");

        FileEntity file = createAndSaveFile(user1, null, "test-123");

        assertThatCode(() -> fileService.deleteTempMeta(file.getId(), "test-123", user2))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 게시글_파일_매핑_성공() {
        UserEntity user1 = createAndSaveUser("3");

        FileEntity file1 = createAndSaveFile(user1, null, "test-123");
        FileEntity file2 = createAndSaveFile(user1, null, "test-123");

        BoardEntity board = createAndSaveBoard(user1);

        fileService.attachDraftFilesToBoard(user1, board, "test-123");

        // 매핑 성공
        assertThat(file1.getBoard()).isEqualTo(board);
        assertThat(file2.getBoard()).isEqualTo(board);

        assertThat(file1.getDraftId()).isEqualTo(null);
    }

    @Test
    void 게시글이_매핑된_파일_삭제() {
        UserEntity user1 = createAndSaveUser("3");
        BoardEntity board = createAndSaveBoard(user1);
        FileEntity file1 = createAndSaveFile(user1, board, null);

        // 삭제 전
        Optional<FileEntity> deleted = fileRepository.findById(file1.getId());
        assertThat(deleted).isNotEmpty();

        fileService.deleteMeta(file1.getId(), board.getId(), user1);

        // 삭제 후
        deleted = fileRepository.findById(file1.getId());
        assertThat(deleted).isEmpty();
    }
}
