package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name= "board")
@Entity
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private String title;
    private String content;

    @Column(nullable = false)
    private LocalDateTime upload_at;
    private LocalDateTime update_at;

    @Builder
    public BoardEntity(UserEntity user_id, CategoryEntity category_id, String title, String content
    , LocalDateTime upload_at) {
        this.user = user_id;
        this.category = category_id;
        this.title = title.trim();
        this.content = (content != null) ? content : ""; //content가 null이 아니면 content, 맞다면 ""
        this.upload_at= LocalDateTime.now();
        this.update_at = LocalDateTime.now();
    }

    //게시판 수정 (카테고리, 제목, 내용, 수정일)
    public void updateBoard(CategoryEntity category, BoardRequest request) {
        this.category = category;
        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new BadRequestException("제목을 입력해 주세요.");
        this.title = request.getTitle().trim();
        this.content = request.getContent();
        this.update_at = LocalDateTime.now();
        //프론트에서 기존 내용+변경 내용 같이 보냄 (변경 안 된 필드는 기존 필드로)
    }

    //카테고리 수정, 카테고리 삭제로 인한 변경 시 업데이트 일자 갱신X
    public void updateCategory(CategoryEntity category_id) {
        this.category = category_id;
    }
}
