package com.studylog.project.plan;

import com.studylog.project.global.CommonUtil;
import com.studylog.project.global.CommonValidator;
import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.ScrollResponse;
import com.studylog.project.global.response.SuccessResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/plans")
@Tag(name="Plan", description = "Plan API, 모든 요청 access token 필요")
public class PlanController {
    private final PlanService planService;

    /*계획 여러 유형 조회
    - 유저는 필수
     1. date별 조회
     2. 카테고리별 조회
     3. 상태별 조회
     4. 키워드별 조회
     다 섞어서도 가능..*/
    @Operation(summary = "계획 목록 조회 (범위 조회 시, 메시지 포함)", description = "정렬(sort) 기본 값: 시작일자/카테고리명 오름차순")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
                    array = @ArraySchema(schema= @Schema(implementation = ScrollPlanResponse.class))))
    @GetMapping("/search")
    public ResponseEntity<ScrollPlanResponse> searchTablePlans(@RequestParam(required = false) LocalDate startDate,
                                                               @RequestParam(required = false) LocalDate endDate,
                                                               @RequestParam(required = false) String category,
                                                               @RequestParam(required = false) String keyword,
                                                               @RequestParam(name="status", required = false) String statusStr,
                                                               @RequestParam(required = false) List<String> sort,
                                                               @RequestParam(required = false) int page,
                                                               @AuthenticationPrincipal CustomUserDetail user) {

        CommonValidator.validatePage(page);

        if (sort == null || sort.isEmpty()) {
            sort = List.of("date,desc", "category,asc");
        } else {
            CommonValidator.validateSort(sort, 2);
        }

        List<Long> categoryList = new ArrayList<>();
        Boolean status = null; //null값 필요해서 객체 타입으로

        CommonValidator.validateDate(startDate, endDate);

        if (category != null && !category.trim().isEmpty()) {
            //여기 안 들어가면 categoryList는 null? ㄴㄴ 내가 위에 빈 리스트 집어넣음 .isEmpty로 검사
            log.info("category: {}", category);
            categoryList = CommonUtil.parseAndValidateCategory(category);
        }
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            //입력했으면 검사
            status = parseStatus(statusStr.trim().toLowerCase());
        }

        keyword = (keyword == null) ? null : keyword.trim(); //공백 제거

        ScrollPlanResponse response= planService.searchTablePlans(user.getUser(), startDate, endDate,
                categoryList, keyword, status, sort, page);
        return ResponseEntity.ok(response); //빈 리스트도 보내짐
    }

    //main용
    @Operation(summary = "메인페이지 todo 조회")
    @GetMapping("/daily")
    //HTTP 응답 바디 타입을 T로 지정해서 제네릭 사용 (타입 안정성 보장)
    public ResponseEntity<ScrollPlanResponse> getDailyPlans(@AuthenticationPrincipal CustomUserDetail user,
                                                            @RequestParam(required = false) int page,
                                                            @RequestParam(required = false) LocalDate startDate){
        CommonValidator.validatePage(page);
        if(startDate == null || !startDate.equals(LocalDate.now())) {
            log.info("잘못된 날짜 값 - startDate {}", startDate);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return ResponseEntity.ok(planService.MainDailyPlans(user.getUser(), startDate, page));
    }

    //main & plan창
    @Operation(summary = "캘린더형 조회")
    @GetMapping("/calender")
    public ResponseEntity<List<CalenderPlanResponse>> getCalenderPlans(@RequestParam(required = false) LocalDate startDate,
                                                                 @RequestParam(required = false) LocalDate endDate,
                                                                 @RequestParam(required = false) String range,
                                                                 @AuthenticationPrincipal CustomUserDetail user){
        if (range == null || startDate == null || endDate == null) {
            throw new CustomException(ErrorCode.DATE_RANGE_REQUIRED);
        }

        return ResponseEntity.ok(planService.getCalenderPlans(startDate, endDate, range, user.getUser()));
    }

    //계획 하나 조회
    @Operation(summary = "계획 단일 조회")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = PlanResponse.class))),
        @ApiResponse(responseCode = "404", description = "조회 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 계획입니다.\"\n}")))
    })
    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long planId,
                                                @AuthenticationPrincipal CustomUserDetail user) {
        PlanResponse plan= planService.getPlan(planId, user.getUser());
        return ResponseEntity.ok(plan);
    }

    //계획 등록
    @Operation(summary = "계획 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "계획 등록 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": true,\n  \"message\": \"계획이 저장되었습니다.\"\n}"))),
        @ApiResponse(responseCode = "404", description = "계획 등록 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"없는 카테고리 지정- 존재하지 않는 카테고리입니다.\"\n}")))
    })
    @PostMapping("")
    public ResponseEntity<SuccessResponse<Void>> setPlan(@Valid @RequestBody PlanRequest request,
                                                  @AuthenticationPrincipal CustomUserDetail user) {
        planService.addPlan(request, user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("계획이 저장되었습니다."));
    }

    //계획 상태 수정
    @Operation(summary = "계획 상태 수정")
    @PatchMapping("/{planId}/complete")
    public ResponseEntity<SuccessResponse<Void>> setPlanStatus(@PathVariable Long planId,
                                                        @RequestParam("status") String status,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        boolean isComplete= parseStatus(status.trim().toLowerCase());
        planService.updateStatus(planId, isComplete, user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("계획 상태가 변경되었습니다."));
    }

    //계획 수정
    @Operation(summary = "계획 수정")
    @PatchMapping("/{planId}")
    public ResponseEntity<SuccessResponse<Void>> updatePlan(@PathVariable Long planId,
                                                     @Valid @RequestBody PlanRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        planService.updatePlan(planId, request, user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("계획이 수정되었습니다."));
    }

    //계획 삭제
    @Operation(summary = "계획 삭제")
    @DeleteMapping("/{planId}")
    public ResponseEntity<SuccessResponse<Void>> deletePlan(@PathVariable Long planId,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        planService.deletePlan(planId, user.getUser());
        return ResponseEntity.ok(SuccessResponse.of("계획이 삭제되었습니다."));
    }

    //계획 조회 (타이머 목록바용)
    @Operation(summary = "타이머 목록바용 계획 조회")
    @GetMapping("/for-timer")
    public ResponseEntity<ScrollResponse<PlansForTimerResponse>> getPlansForTimer(@RequestParam(required = false) LocalDate startDate,
                                                                                      @RequestParam(required = false) LocalDate endDate,
                                                                                      @RequestParam(required = false) String keyword,
                                                                                      @RequestParam(required = false) String sort,
                                                                                      @RequestParam(required = false) int page,
                                                                                      @AuthenticationPrincipal CustomUserDetail user){
        CommonValidator.validatePage(page);

        sort= sort == null? "desc":sort.trim().toLowerCase();

        keyword= keyword == null? null:keyword.trim();

        CommonValidator.validateDate(startDate, endDate);

        return ResponseEntity.ok(planService.getPlansForTimer(startDate, endDate, keyword, sort, page, user.getUser()));
    }

    //status 파싱
    private boolean parseStatus(String statusStr) {
        List<String> validList = List.of("0", "false", "1", "true");

        if (statusStr == null || !validList.contains(statusStr)) {
            log.info("잘못된 상태 값: {}", statusStr);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        switch (statusStr) {
            case "0", "false" -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }
}
