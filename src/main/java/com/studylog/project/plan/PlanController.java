package com.studylog.project.plan;

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

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("study-log/plans")
public class PlanController {
    private final PlanService planService;

    //전체 계획 조회
    @GetMapping("")
    public ResponseEntity<List<PlanResponse>> getPlans(@AuthenticationPrincipal CustomUserDetail user) {
        List<PlanResponse> planList= planService.getPlans(user.getUser());
        return ResponseEntity.ok(planList);
    }

    //계획 일별 조회

    //계획 하나 조회
    @GetMapping("{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long planId,
                                                @AuthenticationPrincipal CustomUserDetail user) {
        PlanResponse plan= planService.getPlan(planId, user.getUser());
        return ResponseEntity.ok(plan);
    }
    //카테고리별 계획 조회

    //계획 선택 조회

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
            throw new BadRequestException("올바르지 않은 입력 값입니다.");

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
                throw new BadRequestException("올바르지 않은 입력 값입니다.");
        }
    }
}
