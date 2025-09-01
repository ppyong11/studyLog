package com.studylog.project.board;

import com.studylog.project.category.CategoryEntity;
import com.studylog.project.user.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    //엔티티로 파라미터 넘겨도 알아서 카테고리 PK값으로 찾아짐
    List<BoardEntity> findByCategory(CategoryEntity categoryEntity);
    void deleteAllByUser(UserEntity user);
    Optional<BoardEntity> findByUserAndId(UserEntity user, Long id);

    @Modifying //변경 쿼리라는 거 알리기
    @Query("UPDATE BoardEntity b SET b.category= :defaultCategory WHERE b.category= :deleteCategory")
    void updateCategory(@Param("deleteCategory") CategoryEntity deleteCategory,
                        @Param("defaultCategory") CategoryEntity defaultCategory);
}
