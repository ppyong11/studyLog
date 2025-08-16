package com.studylog.project.plan;

import com.studylog.project.global.CommonUtil;
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

import java.time.DayOfWeek;
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
                    array = @ArraySchema(schema= @Schema(implementation = PlanDetailResponse.class))))
    @GetMapping("/search")
    public ResponseEntity<PlanDetailResponse> searchPlans( @RequestParam(required = false) String range,
                                          @RequestParam(required = false) LocalDate startDate,
                                          @RequestParam(required = false) LocalDate endDate,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(name="status", required = false) String statusStr,
                                          @RequestParam(required = false) List<String> sort,
                                          @AuthenticationPrincipal CustomUserDetail user) {
        //range 있으면 달성률+메시지 함께 반환
        //없으면 Plans만 반환
        range= range == null? null:range.trim().toLowerCase();
        if(range != null) {
            //공통
            if(startDate == null || endDate == null)
                throw new BadRequestException("조회 범위를 지정한 경우, 시작일과 종료일은 필수입니다.");

            switch (range){
                case "day" -> {
                    if(!startDate.equals(endDate)) throw new BadRequestException("일간 조회는 시작, 종료 일자가 같아야 합니다.");
                }
                case "week" -> {
                    if(startDate.getDayOfWeek() != DayOfWeek.MONDAY) throw new BadRequestException("주간 조회는 시작 요일이 월요일이어야 합니다");
                    if(!endDate.equals(startDate.plusDays(6))) throw new BadRequestException("주간 조회는 월~일(7일) 범위여야 합니다.");
                }
                case "month" -> {
                    if(startDate.getDayOfMonth() != 1) throw new BadRequestException("월간 조회는 시작일이 1일이어야 합니다.");
                    LocalDate monthEndDate= startDate.withDayOfMonth(startDate.lengthOfMonth());
                    if(!endDate.equals(monthEndDate)) throw new BadRequestException("월간 조회는 해당 월의 마지막 날까지 조회해야 합니다.");
                }
                default -> throw new BadRequestException("조회 범위는 day, week, month 중 하나여야 합니다."); //"" 포함 이상한 값 다 걸림
            }
        }

        List<Long> categoryList = new ArrayList<>();
        Boolean status = null; //null값 필요해서 객체 타입으로
        if (sort == null || sort.isEmpty()) { //null or 빈 리스트
            sort = List.of("date,asc", "category,asc"); //기본값 설정
        }
        if (endDate != null && startDate == null) {
            //종료 일자 입력됐으면 시작 일자는 필수
            throw new BadRequestException("시작 일자는 필수 입력 값입니다.");
        }
        if (startDate != null && endDate != null) { //둘 다 입력됐을 때
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException("시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
            }
        }
        if (category != null && !category.trim().isEmpty()) {
            //여기 안 들어가면 categoryList는 null? ㄴㄴ 내가 위에 빈 리스트 집어넣음 .isEmpty로 검사
            log.info("category: {}", category);
            categoryList = CommonUtil.parseAndValidateCategory(category);
        }
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            //입력했으면 검사
            status = parseStatus(statusStr);
        }
        log.info("status 값: {}", status);
        keyword = (keyword == null) ? null : keyword.trim(); //공백 제거

        //바디에 end 값 없으면 null 들어감 (start~전체 일정, end도 설정해야 당일/start~end 일정 나옴)
        //start 값 없으면 당일로 설정
        if(startDate == null) startDate= LocalDate.now();

        PlanDetailResponse response= planService.searchPlans(user.getUser(), startDate, endDate,
                categoryList, keyword, status, sort, range);
        return ResponseEntity.ok(response); //빈 리스트도 보내짐
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
    public ResponseEntity<CommonResponse> setPlan(@Valid @RequestBody PlanRequest request,
                                                  @AuthenticationPrincipal CustomUserDetail user) {
        planService.addPlan(request, user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "계획이 저장되었습니다."));
    }

    //계획 상태 수정
    @Operation(summary = "계획 상태 수정")
    @PatchMapping("{planId}/status")
    public ResponseEntity<CommonResponse> setPlanStatus(@PathVariable Long planId,
                                                        @RequestParam("status") String statusStr,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        log.info(statusStr); //" fAlse "
        boolean status= parseStatus(statusStr);
        planService.updateStatus(planId, status, user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "계획 상태가 변경되었습니다."));
    }

    //계획 수정
    @Operation(summary = "계획 수정")
    @PatchMapping("{planId}")
    public ResponseEntity<CommonResponse> updatePlan(@PathVariable Long planId,
                                                     @Valid @RequestBody PlanRequest request,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        planService.updatePlan(planId, request, user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "계획이 수정되었습니다."));
    }

    //계획 삭제
    @Operation(summary = "계획 삭제")
    @DeleteMapping("{planId}")
    public ResponseEntity<CommonResponse> deletePlan(@PathVariable Long planId,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        planService.deletePlan(planId, user.getUser());
        return ResponseEntity.ok(new CommonResponse(true, "계획이 삭제되었습니다."));
    }

    //status 파싱
    private boolean parseStatus(String statusStr) {
        if (statusStr == null)
            throw new BadRequestException("완료 여부 값이 올바르지 않습니다.");

        String normalized= statusStr.trim().toLowerCase();
        log.info("파싱 메서드 {}", normalized);
        switch (normalized) {
            case "0", "false" -> {
                return false;
            }
            case "1", "true" -> {
                return true;
            }
            //그 외 값들
            default ->
                throw new BadRequestException("완료 여부 값이 올바르지 않습니다.");
        }
    }
}
