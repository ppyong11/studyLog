package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
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
    private Long board_id;

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
        this.title = title;
        this.content = (content != null) ? content : ""; //content가 null이 아니면 content, 맞다면 ""
        this.upload_at= upload_at;
    }

    //게시판 수정 (카테고리, 제목, 내용, 수정일)
    public void updateBoard(CategoryEntity id, String title, String content, LocalDateTime uploadAt) {
        this.category = id;
        this.title = title;
        this.content = content;
        this.upload_at = uploadAt;
        //프론트에서 기존 내용+변경 내용 같이 보냄 (변경 안 된 필드는 기존 필드로)
    }

    //카테고리 수정
    public void updateCategory(CategoryEntity id, LocalDateTime uploadAt) {
        this.category = id;
        this.upload_at = uploadAt;
    }
}
