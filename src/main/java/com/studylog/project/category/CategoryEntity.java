package com.studylog.project.category;

import com.studylog.project.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Table(
    name= "category",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "category_name"})}
)
@Entity
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String category_name;

    @Builder
    public CategoryEntity(UserEntity user_id, String category_name) {
        this.user = user_id;
        this.category_name = category_name;
    }

    public void setCategory_name(String name) {
        this.category_name = name;
    }
}
