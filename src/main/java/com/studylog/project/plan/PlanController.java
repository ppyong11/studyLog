package com.studylog.project.plan;

import com.studylog.project.global.CommonUtil;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService planService;

    /*계획 여러 유형 조회
    - 유저는 필수
     1. date별 조회
     2. 카테고리별 조회
     3. 상태별 조회
     4. 키워드별 조회
     다 섞어서도 가능..*/
    @GetMapping("/search")
    public ResponseEntity<List<PlanResponse>> searchPlans(@RequestParam(required = false) LocalDate startDate,
                                                          @RequestParam(required = false) LocalDate endDate,
                                                          @RequestParam(required = false) String category,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(name="status", required = false) String statusStr,
                                                          @RequestParam(required = false) List<String> sort,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        List<Long> categoryList = new ArrayList<>();
        Boolean status = null; //null값 필요해서 객체 타입으로
        if (sort == null || sort.isEmpty()) { //null or 빈 리스트
            sort = List.of("date,asc", "category,asc"); //기본값 설정
        }
        if (endDate != null && startDate == null) {
            //종료 일자 입력됐으면 시작 일자는 필수
            throw new BadRequestException("시작 일자는 필수 입력 값입니다.");
        }
        if (startDate != null && endDate != null) {
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
        List<PlanResponse> planList = planService.searchPlans(user.getUser(), startDate, endDate,
                categoryList, keyword, status, sort);
        return ResponseEntity.ok(planList); //빈 리스트도 보내짐
        }



    //계획 하나 조회
    @GetMapping("{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long planId,
                                                @AuthenticationPrincipal CustomUserDetail user) {
        PlanResponse plan= planService.getPlan(planId, user.getUser());
        return ResponseEntity.ok(plan);
    }

    //계획 등록
    @PostMapping("")
    public ResponseEntity<ApiResponse> setPlan(@Valid @RequestBody PlanRequest request,
                                               @AuthenticationPrincipal CustomUserDetail user) {
        planService.addPlan(request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "계획이 저장되었습니다."));
    }

    //계획 상태 수정
    @PatchMapping("{planId}/status")
    public ResponseEntity<ApiResponse> setPlanStatus(@PathVariable Long planId,
                                                     @RequestParam("status") String statusStr,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        log.info(statusStr); //" fAlse "
        boolean status= parseStatus(statusStr);
        planService.updateStatus(planId, status, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "계획 상태가 변경되었습니다."));
    }

    //계획 수정
    @PatchMapping("{planId}")
    public ResponseEntity<ApiResponse> updatePlan(@PathVariable Long planId,
                                                  @Valid @RequestBody PlanRequest request,
                                                  @AuthenticationPrincipal CustomUserDetail user) {
        planService.updatePlan(planId, request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "계획이 수정되었습니다."));
    }

    //계획 삭제
    @DeleteMapping("{planId}")
    public ResponseEntity<ApiResponse> deletePlan(@PathVariable Long planId,
                                                  @AuthenticationPrincipal CustomUserDetail user) {
        planService.deletePlan(planId, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "계획이 삭제되었습니다."));
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
