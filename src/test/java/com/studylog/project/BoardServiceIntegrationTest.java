package com.studylog.project;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.board.*;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.file.FileEntity;
import com.studylog.project.file.FileRepository;
import com.studylog.project.file.FileService;
import com.studylog.project.fixture.CategoryFixture;
import com.studylog.project.fixture.FileFixture;
import com.studylog.project.fixture.UserFixture;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class BoardServiceIntegrationTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private FileService fileService;

    @Test
    void 게시글_등록_성공() {
        UserEntity user1 = UserFixture.createAndSaveUser("3", userRepository);
        UserEntity user2 = UserFixture.createAndSaveUser("4", userRepository);

        CategoryEntity category = CategoryFixture.createAndSaveCategory(user1, categoryRepository);

        BoardRequest request = new BoardRequest(category.getId(), "테스트 게시글", "테스트");

        FileEntity file1 = FileFixture.createAndSaveFile(user1, null, "test-123", fileRepository);
        // 유저 동일, draftId 다름
        FileEntity file2 = FileFixture.createAndSaveFile(user1, null, "test-124", fileRepository);
        // 유저 다름, draftId 동일
        FileEntity file3 = FileFixture.createAndSaveFile(user2, null, "test-123", fileRepository);

        BoardDetailResponse response = boardService.createBoard(request, "test-123", user1);

        assertThat(file1.getBoard().getId()).isEqualTo(response.getBoard().getId());
        assertThat(response.getFiles().size()).isEqualTo(1);
    }
}
