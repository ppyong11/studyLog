package com.studylog.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Table(name= "board")
@Entity
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long board_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private UserEntity user_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "category_id", nullable = false)
    private CategoryEntity category_id;

    @Column(nullable = false)
    private String title;
    private String content;

    @Column(nullable = false)
    private LocalDateTime upload_at;
    private LocalDateTime update_at;

    @Builder
    public BoardEntity(UserEntity user_id, CategoryEntity category_id, String title, String content,
                       LocalDateTime upload_at, LocalDateTime update_at) {
        this.user_id = user_id;
        this.category_id = category_id;
        this.title = title;
        this.content = content;
        this.upload_at = upload_at;
        this.update_at = update_at;
    }
}
