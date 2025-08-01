package com.studylog.project.category;

import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    //카테고리 전체&키워드 조회
    @GetMapping("search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(@RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false, defaultValue = "asc") String sort,
                                                                   @AuthenticationPrincipal CustomUserDetail user) {
        keyword = keyword == null ? null : keyword.trim();
        sort= sort.trim().toLowerCase(); //공백 제거 & 소문자 (null이 될 리 X)
        log.info("sort {}", sort);
        if (!sort.equals("asc") && !sort.equals("desc")) {
            throw new BadRequestException("지원하지 않는 정렬입니다.");
        }
        List<CategoryResponse> categoryList= categoryService.searchCategories(keyword, sort, user.getUser());
        return ResponseEntity.ok(categoryList);
    }

    //카테고리 단일 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long categoryId,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        CategoryResponse response = categoryService.getCategory(categoryId, user.getUser());
        return ResponseEntity.ok(response);
    }

    //카테고리 추가
    @PostMapping("")
    public ResponseEntity<ApiResponse> newCategory(@Valid @RequestBody CategoryRequest request,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.addCategory(request, user.getUser());
        return ResponseEntity.ok(new ApiResponse( true, "카테고리가 등록되었습니다."));
    }

    //카테고리 수정
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryRequest request,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.updateCategory(categoryId, request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(true, "카테고리가 수정되었습니다."));
    }

    //카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> delCategory(@PathVariable Long categoryId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.delCategory(categoryId, user.getUser());
        return ResponseEntity.ok(new ApiResponse( true, "카테고리가 삭제되었습니다."));
    }
}
