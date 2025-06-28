package com.studylog.project.category;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("study-log/plan/categories")
public class CategoryController {
    CategoryService categoryService;
    //카테고리 추가
    @PostMapping("/add-cate")
    public ResponseEntity<ApiResponse> newCategory(@Valid @RequestBody CategoryRequest request,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.addCategory(request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "새 카테고리 등록 완료"));
    }

    //카테고리 수정
    @PatchMapping("/update/{categoryId}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryRequest request,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.updateCategory(categoryId, request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "카테고리 수정 완료"));
    }

    //카테고리 삭제
    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<ApiResponse> delCategory(@PathVariable Long categoryId,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.delCategory(categoryId, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "카테고리 삭제 완료"));
    }
}
