package com.studylog.project.file;

import com.studylog.project.category.CategoryResponse;
import com.studylog.project.global.response.CommonResponse;
import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/files")
@RestController
@Controller
@RequiredArgsConstructor
@Tag(name="File", description = "File API, 모든 요청 access token 필요")
public class FileController {
    private final FileService fileService;

    @Operation(summary = "게시글에 파일 등록 (1건씩 등록)", description = "게시글을 생성하면서 파일을 등록할 시, draftId 필수. 게시글 등록 단계 때 draft Id와 매핑되어 file이 boardId를 갖게 된다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "파일 등록 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = FileResponse.class))),
        @ApiResponse(responseCode = "400", description = "파일 등록 실패",
        content = @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"블랙리스트 확장자- 해당 파일은 업로드할 수 없습니다. / " +
                        "draftId 미입력 (임시 파일 상태)- 파일 업로드에 필요한 값이 없습니다.\"\n}"))),
        @ApiResponse(responseCode = "404", description = "게시글 수정 중 파일 등록 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 게시글입니다.\"\n}"))),
        @ApiResponse(responseCode = "500", description = "서버 문제로 파일 등록 실패",
        content = @Content(mediaType = "application/json",
        schema = @Schema(
              example = "{\n  \"success\": false,\n  \"message\": \"파일 업로드 실패\"\n}")))
    })
    @PostMapping("")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Long boardId,
                                                   @RequestParam(required = false) String draftId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {

        return ResponseEntity.ok(fileService.saveMeta(file, boardId, draftId == null? null:draftId.trim(), user.getUser()));
    }

    @Operation(summary = "front에 파일 띄우기")
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId,
                                             @AuthenticationPrincipal CustomUserDetail user) {
        return fileService.getFileResponse(fileId, user.getUser());
    }

    @Operation(summary = "파일 삭제", description = "삭제할 파일 id와 파일이 있는 게시글 id가 일치해야 하며, 임시 파일이라면 파일의 draft id와 일치해야 한다.")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "파일 삭제 성공",
        content= @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": true,\n  \"message\": \"파일이 삭제되었습니다.\"\n}"))),
    @ApiResponse(responseCode = "400", description = "파일 삭제 실패",
        content = @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"게시글 id 미입력- 삭제할 파일의 게시글을 입력해 주세요. / " +
                        "파일이 등록된 게시글과 일치하지 않습니다. / draftId 미입력 (임시 파일 상태)- 파일 삭제에 필요한 값이 없습니다." +
                        " / draftId 일치 X- 파일 삭제에 필요한 값이 일치하지 않습니다.\"\n}"))),
            /*
            *                 example = "{\n  \"success\": false,\n  \"message\": \"게시글 id 미입력- 삭제할 파일의 게시글을 입력해 주세요. / " +
                        "파일이 등록된 게시글과 일치하지 않습니다. / draftId 미입력 (임시 파일 상태)- 파일 삭제에 필요한 값이 없습니다." +
                        " / draftId 일치 X- 파일 삭제에 필요한 값이 일치하지 않습니다.\"\n}"))),*/
    @ApiResponse(responseCode = "404", description = "게시글 수정 중 파일 등록 실패",
        content = @Content(mediaType = "application/json",
        schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 게시글입니다.\"\n}")))
    })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<CommonResponse> deleteFile(@PathVariable Long fileId,
                                                     @RequestParam(required = false) Long boardId,
                                                     @RequestParam(required = false) String draftId,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        fileService.deleteMeta(fileId, boardId, draftId, user.getUser());
        return ResponseEntity.ok(new CommonResponse(true, "파일이 삭제되었습니다."));
    }
}
