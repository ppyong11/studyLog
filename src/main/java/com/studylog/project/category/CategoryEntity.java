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
    @Column(name= "category_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name="category_name")
    private String name;

    @Builder
    public CategoryEntity(UserEntity user_id, String category_name) {
        this.user = user_id;
        this.name = category_name.trim(); //공백 없애고 저장
    }

    //카테고리 수정
    public void setCategory_name(String name) {
        this.name = name;
    }
}
