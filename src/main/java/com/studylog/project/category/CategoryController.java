package com.studylog.project.category;

import com.studylog.project.global.ScrollResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name="Category", description = "Category API, 모든 요청 access token 필요")
public class CategoryController {
    private final CategoryService categoryService;
    private final Map<String, String> COLORS= Map.of(
            "#F7F7F7", "#484848", //기본
            "#E3E3E3", "#484848", //회색
            "#F1E7E1", "#554539", //갈색
            "#FFE5E5", "##821912", //빨간색
            "#FFE7CD", "#72471D", //주황색
            "#FCF4CC", "#584C12", //노란색
            "#E4EFE7", "#134F14", //초록색
            "#E4F2FD", "#265882", //하늘색
            "#F4F2FF", "#652F79", //보라색
            "#FFEDF5", "#7E1734" //분홍색
    );

    @Operation(summary = "전체 카테고리 조회", description = "front-end 전역상태 관리용")
    @GetMapping("")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(categoryService.getAllCategories(user.getUser()));
    }

    //카테고리 전체&키워드 조회 (페이지)
    @Operation(summary = "카테고리 목록 조회 (리스트)", description = "정렬 (sort) 기본값: 카테고리명 오름차순")
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content= @Content(mediaType = "application/json",
        array = @ArraySchema(schema= @Schema(implementation = CategoryResponse.class))))
    @GetMapping("search")
    public ResponseEntity<ScrollResponse<CategoryResponse>> searchCategories(@RequestParam(required = false) String keyword,
                                                                             @RequestParam(required = false) Integer page,
                                                                             @AuthenticationPrincipal CustomUserDetail user) {

        if(page == null || page < 1) throw new BadRequestException("잘못된 페이지 값입니다.");
        return ResponseEntity.ok(
                categoryService.searchCategories(keyword == null ? null : keyword.trim(), page, user.getUser()));
    }

    //카테고리 단일 조회
    @Operation(summary = "카테고리 단일 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "조회 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 카테고리입니다.\"\n}")))
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long categoryId,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        CategoryResponse response = categoryService.getCategory(categoryId, user.getUser());
        return ResponseEntity.ok(response);
    }

    //카테고리 추가
    @Operation(summary = "카테고리 등록")
    @PostMapping("")
    public ResponseEntity<CommonResponse> newCategory(@Valid @RequestBody CategoryRequest request,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        if(!COLORS.containsKey(request.getBgColor())) throw new BadRequestException("지원하지 않는 색상입니다.");
        categoryService.addCategory(request, COLORS.get(request.getBgColor()), user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "카테고리가 등록되었습니다."));
    }

    //카테고리 수정
    @Operation(summary = "카테고리 수정")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CommonResponse> updateCategory(@PathVariable Long categoryId,
                                                         @Valid @RequestBody CategoryRequest request,
                                                         @AuthenticationPrincipal CustomUserDetail user) {
        if(!COLORS.containsKey(request.getBgColor())) throw new BadRequestException("지원하지 않는 색상입니다.");
        categoryService.updateCategory(categoryId, request, COLORS.get(request.getBgColor()), user.getUser());
        return ResponseEntity.ok(new CommonResponse(true, "카테고리가 수정되었습니다."));
    }

    //카테고리 삭제
    @Operation(summary = "카테고리 삭제")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"카테고리가 삭제되었습니다.\"\n}"))),
        @ApiResponse(responseCode = "400", description = "[기타] 카테고리 삭제 시도로 인한 삭제 실패",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"해당 카테고리는 삭제할 수 없습니다.\"\n}"))),
        @ApiResponse(responseCode = "404", description = "삭제 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 카테고리입니다.\"\n}")))
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CommonResponse> delCategory(@PathVariable Long categoryId,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        categoryService.delCategory(categoryId, user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "카테고리가 삭제되었습니다."));
    }
}
