package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.global.exception.*;
import com.studylog.project.user.UserEntity;
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

import java.io.IOException;
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

    private final FileRepository fileRepository;
    private final Set<String> blackExts= Set.of(
            "exe", "msi", "bat", "cmd", "sh", "bin", "com", "cpl", "scr", "jar",
            "js", "jsp", "php", "asp", "aspx", "cgi", "pl", "py", "rb",
            "dll", "sys", "so", "drv", "vxd", "ocx",
            "pif", "vbs", "vbe", "wsf", "wsh", "ps1",
            "html", "htm", "xml", "svg"
    );

    public String saveFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) return null;

        String originalFileName = multipartFile.getOriginalFilename();
        log.info("업로드된 파일 타입: {}", multipartFile.getContentType());

        int extIndex = originalFileName.lastIndexOf(".");
        if (extIndex == -1) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String ext = originalFileName.substring(extIndex + 1).toLowerCase();

        if (blackExts.contains(ext)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String uuid = UUID.randomUUID().toString();
        String savedName = uuid + "." + ext;

        try {
            // 1. 상대 경로("./uploads/")를 현재 프로젝트의 절대 경로로 변환 *앱폴더/uploads/
            // 배포 시엔 앱 폴더 밖 시스템 경로에 파일 두기 (재배포 시 데이터 안 날아감)
            Path uploadPath = Paths.get(fileDir).toAbsolutePath().normalize();

            // 2. NIO 방식으로 폴더 생성 (현재 프로젝트 폴더의 절대 경로로 변환해줌)
            // 이미 절대 경로면 변환 X
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 최종 파일이 저장될 완벽한 경로 완성
            Path filePath = uploadPath.resolve(savedName);

            // 파일 저장!
            multipartFile.transferTo(filePath.toFile());

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return savedName;
    }

    // 임시 파일 등록
    public void uploadTempFile(MultipartFile multipartFile, String draftId, UserEntity user) {
        String savedName= saveFile(multipartFile);

        //DB에 메타 정보 저장
        FileEntity fileEntity= FileEntity.builder()
                .user(user)
                .board(null) //게시글 등록 전엔 id 안 받음, 수정 중엔 바로 채워짐
                .draft(draftId)
                .size(multipartFile.getSize())
                .path(fileDir + savedName) //서버에 저장된 경로
                .originalName(multipartFile.getOriginalFilename())
                .name(savedName)
                .type(multipartFile.getContentType()) //밈타입+확장자
                .build();
        fileRepository.save(fileEntity);
    }

    public void deleteTempMeta(Long fileId, String draftId, UserEntity user) {
        FileEntity file = fileRepository.findByUserAndIdAndDraftId(user, fileId, draftId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        //임시 파일 draft 값 검증 or 게시글 검증된 파일 삭제 시
        fileRepository.delete(file);
    }

    public void deleteMeta(Long fileId, Long boardId, UserEntity user) {
        FileEntity file = fileRepository.findByUserAndIdAndBoard(user, fileId, boardId)
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

    public ResponseEntity<Resource> getFileResponse(Long fileId, UserEntity user) {
        FileEntity file= getFileEntity(fileId, user);

        String path= file.getPath();
        String contentType= file.getType();
        String originalName= file.getOriginalName();
        Resource resource= new FileSystemResource(path);

        boolean isImage= contentType != null && contentType.startsWith("image/");
        String disposition= isImage? "inline" : "attachment";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + originalName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    public FileEntity getFileEntity(Long fileId, UserEntity user) {
        return fileRepository.findByUserAndId(user, fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }

    @Scheduled(cron= "0 */30 * * * *") //30분마다 시행
    public void deleteDraftFiles(){
        LocalDateTime cutoff= LocalDateTime.now().minusHours(2);
        List<FileEntity> expiredFiles= fileRepository.findAllByUploadAtBeforeAndDraftIdIsNotNull(cutoff);
        fileRepository.deleteAll(expiredFiles);
    }
}
