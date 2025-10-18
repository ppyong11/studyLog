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

    @Column(name="name")
    private String name;

    @Column(name="bg_color")
    private String bgColor;

    @Column(name="text_color")
    private String textColor;

    @Builder
    public CategoryEntity(UserEntity user_id, String name, String bgColor, String textColor) {
        this.user = user_id;
        this.name = name;
        this.bgColor= bgColor;
        this.textColor= textColor;
    }

    //카테고리 수정
    public void updateCategory(CategoryRequest request, String textColor) {
        this.name = request.getName().trim();
        this.bgColor= request.getBgColor();
        this.textColor= textColor;
    }
}
