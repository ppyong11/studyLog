package com.studylog.project.category;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("study-log/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    //카테고리 전체 조회
    @GetMapping("")
    public ResponseEntity<List<CategoryResponse>> getCategories(@AuthenticationPrincipal CustomUserDetail user) {
        List<CategoryResponse> categoryList= categoryService.getCategories(user.getUser());
        return ResponseEntity.ok(categoryList);
    }

    //카테고리 단일 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long categoryId,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        CategoryResponse response = categoryService.getCategory(categoryId, user.getUser());
        return ResponseEntity.ok(response);
    }

    //카테고리에 해당하는 계획 조회

    //카테고리에 해당하는 글 조회

    //카테고리 추가
    @PostMapping("")
    public ResponseEntity<ApiResponse> newCategory(@Valid @RequestBody CategoryRequest request,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.addCategory(request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "새 카테고리 등록 완료"));
    }

    //카테고리 수정
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryRequest request,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.updateCategory(categoryId, request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "카테고리 수정 완료"));
    }

    //카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> delCategory(@PathVariable Long categoryId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.delCategory(categoryId, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "카테고리 삭제 완료"));
    }
}
