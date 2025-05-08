package com.studylog.project.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Table(
    name= "category",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "category_name"})}
)
@Entity
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) //파라미터 안 받음 (setId() 차단)
    private Long category_id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "user_id", nullable = false)
    private UserEntity user_id;

    private String category_name;

    @Builder
    public CategoryEntity(UserEntity user_id, String category_name) {
        this.user_id = user_id;
        this.category_name = category_name;
    }
}
