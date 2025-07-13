package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByUserAndId(UserEntity user, Long id);
    List<FileEntity> findAllByUserAndDraftId(UserEntity user, String draftId);
    List<FileEntity> findAllByUserAndBoard(UserEntity user, BoardEntity board);
    List<FileEntity> findAllByUploadAtBeforeAndDraftTrue(LocalDateTime cutoff);

}
