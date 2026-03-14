package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.global.exception.*;
import com.studylog.project.user.UserEntity;
import com.studylog.project.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor//빈으로 등록
@Transactional
@Slf4j
public class FileService {
    // yml 설정 값 읽어옴
    @Value("${file.upload.dir}")
    private String fileDir;

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final Set<String> blackExts= Set.of(
            "exe", "msi", "bat", "cmd", "sh", "bin", "com", "cpl", "scr", "jar",
            "js", "jsp", "php", "asp", "aspx", "cgi", "pl", "py", "rb",
            "dll", "sys", "so", "drv", "vxd", "ocx",
            "pif", "vbs", "vbe", "wsf", "wsh", "ps1",
            "html", "htm", "xml", "svg"
    );

    // 1. saveFile 수정: 이제 파일 이름이 아닌 '실제 저장된 전체 경로'를 반환합니다.
    public String saveFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) return null;

        String originalFileName = multipartFile.getOriginalFilename();
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        // 블랙리스트 체크 (기존 로직 유지)
        if (blackExts.contains(ext)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String savedName = UUID.randomUUID().toString() + "." + ext;

        try {
            Path uploadPath = Paths.get(fileDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ⭐ resolve를 사용하면 슬래시(/) 누락 걱정이 없습니다.
            Path filePath = uploadPath.resolve(savedName);

            // 파일 물리 저장
            multipartFile.transferTo(filePath.toFile());

            // ⭐ 파일명이 아닌 '전체 절대 경로'를 반환합니다.
            return filePath.toString();

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. uploadTempFile 수정: saveFile에서 준 완벽한 경로를 DB에 넣습니다.
    public FileResponse uploadTempFile(MultipartFile multipartFile, String draftId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        // 이제 fullPath는 "/Users/ahyeon/.../uploads/uuid.png" 처럼 완벽합니다.
        String fullPath = saveFile(multipartFile);

        FileEntity fileEntity = FileEntity.builder()
                .user(proxyUser)
                .draft(draftId)
                .size(multipartFile.getSize())
                .path(fullPath)
                .originalName(multipartFile.getOriginalFilename())
                .name(Paths.get(fullPath).getFileName().toString()) // 파일명만 추출
                .type(multipartFile.getContentType())
                .build();

        return FileResponse.toDto(fileRepository.save(fileEntity));
    }
    public void deleteTempMeta(Long fileId, String draftId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        FileEntity file = fileRepository.findByUserAndIdAndDraftId(proxyUser, fileId, draftId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        //임시 파일 draft 값 검증 or 게시글 검증된 파일 삭제 시
        fileRepository.delete(file);
    }

    public void deleteMeta(Long fileId, Long boardId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);

        FileEntity file = fileRepository.findByUserAndIdAndBoard(proxyUser, fileId, boardId)
                        .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        fileRepository.delete(file);
    }

    public void attachDraftFilesToBoard(UserEntity user, BoardEntity board, String draftId) {
        // 빈 리스트면 동작 X
        List<FileEntity> files = fileRepository.findAllByUserAndDraftId(user, draftId);

        for (FileEntity file : files) {
            file.attachBoard(board);
            file.resetDraftId();
        }
    }

    public ResponseEntity<Resource> getFileResponse(Long fileId, Long userId) {
        UserEntity proxyUser = userRepository.getReferenceById(userId);
        FileEntity file = getFileEntity(fileId, proxyUser);

        String path = file.getPath();
        String contentType = file.getType();
        String originalName = file.getOriginalName();
        Resource resource = new FileSystemResource(path);

        // 한글 파일명 인코딩 처리
        String encodedFileName = UriUtils.encode(originalName, StandardCharsets.UTF_8);

        boolean isImage = contentType != null && contentType.startsWith("image/");
        String disposition = isImage ? "inline" : "attachment";

        return ResponseEntity.ok()
                // filename*="UTF-8''..." 형식을 사용해야 브라우저가 한글을 제대로 인식합니다.
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    public FileEntity getFileEntity(Long fileId, UserEntity user) {
        UserEntity proxyUser = userRepository.getReferenceById(user.getUser_id());

        return fileRepository.findByUserAndId(proxyUser, fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }

    @Scheduled(cron= "0 */30 * * * *") //30분마다 시행
    public void deleteDraftFiles(){
        LocalDateTime cutoff= LocalDateTime.now().minusHours(2);
        List<FileEntity> expiredFiles= fileRepository.findAllByUploadAtBeforeAndDraftIdIsNotNull(cutoff);
        fileRepository.deleteAll(expiredFiles);
    }
}
