package com.studylog.project.file;

import com.studylog.project.board.BoardEntity;
import com.studylog.project.board.BoardRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.FileUploadFailedException;
import com.studylog.project.global.exception.NotFoundException;
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
    private final String fileDir= "/tmp/uploads/";
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

    public FileResponse saveMeta(MultipartFile multipartFile, Long boardId, String draftId, UserEntity user) {
        String savedName= saveFile(multipartFile);
        BoardEntity board= null;
        if(boardId == null && (draftId == null || draftId.isBlank()))
            throw new BadRequestException("파일 업로드에 필요한 값이 없습니다.");
        if (boardId != null) {
            board = boardRepository.findByUserAndId(user, boardId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
            draftId= null;
        }

        //DB에 메타 정보 저장
        FileEntity fileEntity= FileEntity.builder()
                .user(user)
                .board(board) //게시글 등록 전엔 id 안 받음, 수정 중엔 바로 채워짐
                .draft(draftId) //게시글 있을 땐 null
                .size(multipartFile.getSize())
                .path(fileDir + savedName) //서버에 저장된 경로
                .originalName(multipartFile.getOriginalFilename())
                .name(savedName)
                .type(multipartFile.getContentType()) //밈타입+확장자
                .build();
        fileRepository.save(fileEntity);
        return FileResponse.toDto(fileEntity);

    }

    public void deleteMeta(Long fileId, Long boardId, String draftId, UserEntity user) {
        FileEntity file= getFileEntity(fileId, user);
        if(file.getBoard() != null) { //게시글에 등록된 파일을 삭제할 경우
            if(boardId == null) throw new BadRequestException("삭제할 파일의 게시글을 입력해 주세요.");
            BoardEntity board= boardRepository.findByUserAndId(user, boardId)
                    .orElseThrow(()-> new NotFoundException("존재하지 않는 게시글입니다."));
            if(!file.getBoard().getId().equals(board.getId()))
                throw new BadRequestException("파일이 등록된 게시글과 일치하지 않습니다.");
        }
        else{ //임시 파일이라면
            if(draftId == null)  throw new BadRequestException("파일 삭제에 필요한 값이 없습니다.");
            if(!file.getDraftId().equals(draftId))
                throw new BadRequestException("파일 삭제에 필요한 값이 일치하지 않습니다.");
        }
        //임시 파일 draft 값 검증 or 게시글 검증된 파일 삭제 시
        fileRepository.delete(file);
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
                .orElseThrow(() -> new NotFoundException("존재하지 않는 파일입니다."));
    }

    public List<FileEntity> getFilesByUserAndDraftId(UserEntity user, String draftId){
        return fileRepository.findAllByUserAndDraftId(user, draftId);
    }

    public List<FileEntity> getFilesByBoard(UserEntity user, BoardEntity board){
        return fileRepository.findAllByUserAndBoard(user, board);
    }

    @Scheduled(cron= "0 */30 * * * *") //30분마다 시행
    public void deleteDraftFiles(){
        LocalDateTime cutoff= LocalDateTime.now().minusHours(2);
        List<FileEntity> expiredFiles= fileRepository.findAllByUploadAtBeforeAndDraftTrue(cutoff);
        fileRepository.deleteAll(expiredFiles);
    }
}
