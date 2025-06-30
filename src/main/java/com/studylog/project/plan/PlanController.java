package com.studylog.project.plan;

import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("study-log/plans")
public class PlanController {
    private final PlanService planService;
    //계획 등록
    @PostMapping("")
    public ResponseEntity<ApiResponse> setPlan(@Valid @RequestBody PlanCreateRequest request,
                                               @AuthenticationPrincipal CustomUserDetail user) {
        planService.addPlan(request, user.getUser());
        return ResponseEntity.ok(new ApiResponse(200, true, "계획이 저장되었습니다."));
    }
}
