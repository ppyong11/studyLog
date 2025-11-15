package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.global.exception.*;
import com.studylog.project.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor//빈으로 등록
@Transactional
@Slf4j
public class FileService {
    private final String fileDir= "/home/ubuntu/app-data/uploads/";
    private final FileRepository fileRepository;
    private final BoardRepository boardRepository;
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
        String uuid= UUID.randomUUID().toString();
        //.확장자 부분 받아옴
        log.info(multipartFile.getContentType());
        String ext= originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if(blackExts.contains(ext)) throw new BadRequestException("해당 파일은 업로드할 수 없습니다.");
        String savedName= uuid + ext;
        try {
            File file= new File(fileDir + savedName); //데이터 X, 파일 객체
            //파일을 tmp/uploads/랜덤명에 저장
            multipartFile.transferTo(file); //multipartFile: 데이터
        } catch (IOException e) {
            throw new FileUploadFailedException("파일 업로드 실패");
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
        List<FileEntity> expiredFiles= fileRepository.findAllByUploadAtBeforeAndDraftIsNotNull(cutoff);
        fileRepository.deleteAll(expiredFiles);
    }
}
