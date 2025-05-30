package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name= "file")
@Entity
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long file_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @Column(nullable = false)
    private String file_path;

    @Column(nullable = false)
    private String file_name;

    @Column(nullable = false)
    private String file_type;

    @Column(nullable = false)
    private LocalDateTime upload_at;

    @Builder
    public FileEntity(UserEntity user_id, BoardEntity board_id, String file_path, String file_name,
                      String file_type, LocalDateTime upload_at) {
        this.user = user_id;
        this.board = board_id;
        this.file_path = file_path;
        this.file_name = file_name;
        this.file_type = file_type;
        this.upload_at = upload_at;
    }

}
