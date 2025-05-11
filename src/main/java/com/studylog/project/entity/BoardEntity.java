package com.studylog.project.entity;

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
    public BoardEntity(UserEntity user_id, CategoryEntity category_id, String title, String content,
                       LocalDateTime upload_at, LocalDateTime update_at) {
        this.user = user_id;
        this.category = category_id;
        this.title = title;
        this.content = content;
        this.upload_at = upload_at;
        this.update_at = update_at;
    }
}
