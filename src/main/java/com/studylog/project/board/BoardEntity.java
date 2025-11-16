package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.file.FileEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Table(name= "board")
@Entity
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long id;

    @ManyToOne()
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

    //FileEntity의 board_id를 참조해서 BoardEntity의 files 리스트에 들어가는 구조
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE) //board 삭제 시 file 삭제
    private List<FileEntity> files= new ArrayList<>();

    @Builder
    public BoardEntity(UserEntity user_id, CategoryEntity category_id, String title, String content) {
        this.user = user_id;
        this.category = category_id;
        this.title = title;
        this.content = (content != null) ? content : ""; //content가 null이 아니면 content, 맞다면 ""
        this.upload_at= LocalDateTime.now();
        this.update_at = LocalDateTime.now();
    }

    //게시판 수정 (카테고리, 제목, 내용, 수정일)
    public void updateBoard(CategoryEntity category, BoardRequest request) {
        this.category = category;
        this.title = request.title();
        this.content = request.content();
        this.update_at = LocalDateTime.now();
        //프론트에서 기존 내용+변경 내용 같이 보냄 (변경 안 된 필드는 기존 필드로)
    }

    //카테고리 수정, 카테고리 삭제로 인한 변경 시 업데이트 일자 갱신X
    public void updateCategory(CategoryEntity category_id) {
        this.category = category_id;
    }
}
