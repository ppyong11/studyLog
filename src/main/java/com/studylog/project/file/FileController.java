package com.studylog.project.file;

import com.studylog.project.board.BoardService;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("study-log/files")
@RestController
@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Long boardId,
                                                   @RequestParam(required = false) String draftId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {

        return ResponseEntity.ok(fileService.saveMeta(file, boardId, draftId == null? null:draftId.trim(), user.getUser()));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId,
                                             @AuthenticationPrincipal CustomUserDetail user) {
        return fileService.getFileResponse(fileId, user.getUser());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable Long fileId,
                                                  @RequestParam(required = false) Long boardId,
                                                  @RequestParam(required = false) String draftId,
                                                  @AuthenticationPrincipal CustomUserDetail user) {
        fileService.deleteMeta(fileId, boardId, draftId, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "파일이 삭제되었습니다."));
    }
}
