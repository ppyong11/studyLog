package com.studylog.project.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.studylog.project.global.CommonUtil;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("study-log/boards")
@Slf4j
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoards(@PathVariable("boardId") Long id,
                                   @AuthenticationPrincipal CustomUserDetail user) {
        BoardDetailResponse response= boardService.getBoard(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BoardResponse>> searchBoards(@RequestParam(required = false) String category,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) List<String> sort,
                                                            @AuthenticationPrincipal CustomUserDetail user) {
        List<Long> categoryList = new ArrayList<>(); //빈 리스트
        //플랜도 바꾸기 (sort)
        if (sort == null || sort.isEmpty()) { //null or 빈 리스트
            sort = List.of("title,asc", "category,asc"); //기본값 설정
        }
        log.info("sort: {}", sort.size());
        if (category != null && !category.trim().isEmpty()) {
            categoryList = CommonUtil.parseAndValidateCategory(category);
        }
        keyword = (keyword == null) ? null : keyword.trim();

        List<BoardResponse> response = boardService.searchBoards(categoryList, keyword, sort, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<BoardDetailResponse> createBoard(@Valid @RequestBody BoardCreateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        BoardDetailResponse response= boardService.createBoard(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{boardId}")
    public ResponseEntity<BoardDetailResponse> updateBoard(@PathVariable("boardId") Long id,
                                                     @Valid @RequestBody BoardUpdateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(boardService.updateBoard(id, request, user.getUser()));
    }

    @DeleteMapping("{boardId}")
    public ResponseEntity<ApiResponse> deleteBoard(@PathVariable("boardId") Long boardId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        boardService.deleteBoard(boardId, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "게시글이 삭제되었습니다."));
    }
}
