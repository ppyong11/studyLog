package com.studylog.project.file;

import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    // user, fileId, boardId 동일한 파일 찾기
    @Query("select f from FileEntity f where f.user= :user and f.id= :fileId and f.board.id= :boardId")
    Optional<FileEntity> findByUserAndIdAndBoard(@Param("user") UserEntity user,
                                                 @Param("fileId") Long id,
                                                 @Param("boardId") Long boardId);

    Optional<FileEntity> findByUserAndId(UserEntity user, Long id);
    Optional<FileEntity> findByUserAndIdAndDraftId(UserEntity user, Long id, String draftId);
    List<FileEntity> findAllByUserAndDraftId(UserEntity user, String draftId);
    List<FileEntity> findAllByUploadAtBeforeAndDraftIdIsNotNull(LocalDateTime cutoff);

}
