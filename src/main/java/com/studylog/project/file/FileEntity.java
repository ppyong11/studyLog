package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name= "file")
@Entity
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="file_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name= "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    @Column(name= "draft_id")
    private String draftId;

    @Column(name="size", nullable = false)
    private Long size;

    @Column(name= "path", nullable = false)
    private String path;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name= "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name= "upload_at", nullable = false)
    private LocalDateTime uploadAt;

    @Builder
    public FileEntity(UserEntity user,BoardEntity board, String draft, Long size, String path, String originalName,
                      String name, String type) {
        this.user = user;
        this.board = board; //임시 파일은 board == null (null 들어가도 오류 안남)
        this.draftId= draft;
        this.size = size;
        this.path = path;
        this.originalName = originalName;
        this.name = name;
        this.type = type;
        this.uploadAt = LocalDateTime.now();
    }

    @Builder(builderMethodName = "testBuilder")
    public FileEntity(Long id, UserEntity user, BoardEntity board, String draftId) {
        this.id = id;
        this.user = user;
        this.board = board;
        this.draftId = draftId;
    }

    public void attachBoard(BoardEntity board) {
        this.board = board;
        board.getFiles().add(this);
    }

    public void resetDraftId(){
        this.draftId = null; //임시 파일 벗어남
    }
}
