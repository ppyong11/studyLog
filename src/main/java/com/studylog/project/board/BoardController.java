package com.studylog.project.board;

import com.studylog.project.global.CommonUtil;
import com.studylog.project.global.PageResponse;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.response.CommonResponse;
import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@Slf4j
@RequiredArgsConstructor
@Tag(name="Board", description = "Board API, 모든 요청 access token 필요")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "게시글 단일 조회 (파일 목록 포함)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = BoardDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "조회 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 게시글입니다.\"\n}")))
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoards(@PathVariable("boardId") Long id,
                                   @AuthenticationPrincipal CustomUserDetail user) {
        BoardDetailResponse response= boardService.getBoard(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 목록 조회 (리스트 형태)", description = "정렬(sort) 기본 값: 게시글명/카테고리명 오름차순")
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content= @Content(mediaType = "application/json",
        array = @ArraySchema(schema= @Schema(implementation = BoardResponse.class))))
    @GetMapping("/search")
    public ResponseEntity<PageResponse<BoardResponse>> searchBoards(@RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) List<String> sort,
                                                     @RequestParam(required = false) Integer page,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        if (page == null || page < 1)
            throw new BadRequestException("잘못된 페이지 값입니다.");
        List<Long> categoryList = new ArrayList<>(); //빈 리스트

        if(sort != null && sort.size() != 3){
            throw new BadRequestException("잘못된 정렬 값입니다.");
        }

        if (sort == null) { //null or 빈 리스트
            sort = List.of("date,desc", "category,asc", "title,asc"); //기본값 설정
        }
        log.info("sort: {}", sort.size());
        if (category != null && !category.trim().isEmpty()) {
            categoryList = CommonUtil.parseAndValidateCategory(category);
        }
        keyword = (keyword == null) ? null : keyword.trim();

        return ResponseEntity.ok(boardService.searchBoards(categoryList, keyword, sort, page, user.getUser()));
    }

    @Operation(summary = "게시글 등록", description = "임시 파일 매핑을 위한 draftId 필수")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 등록 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = BoardDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "게시글 등록 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"draftId 미입력- 게시글 고유 값이 없습니다.\"\n}"))),
        @ApiResponse(responseCode = "404", description = "없는 카테고리 입력으로 인한 게시글 등록 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 카테고리입니다.\"\n}")))

    })
    @PostMapping("")
    public ResponseEntity<BoardDetailResponse> createBoard(@Valid @RequestBody BoardCreateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        BoardDetailResponse response= boardService.createBoard(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 수정")
    @PatchMapping("{boardId}")
    public ResponseEntity<BoardDetailResponse> updateBoard(@PathVariable("boardId") Long id,
                                                     @Valid @RequestBody BoardUpdateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(boardService.updateBoard(id, request, user.getUser()));
    }

    @Operation(summary = "게시글 삭제 (해당 게시글의 파일 함께 삭제)")
    @DeleteMapping("{boardId}")
    public ResponseEntity<CommonResponse> deleteBoard(@PathVariable("boardId") Long boardId,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        boardService.deleteBoard(boardId, user.getUser());
        return ResponseEntity.ok(new CommonResponse(true, "게시글이 삭제되었습니다."));
    }
}
