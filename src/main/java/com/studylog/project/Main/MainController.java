package com.studylog.project.Main;

import com.studylog.project.jwt.CustomUserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
@Tag(name="Main", description = "메인화면 API, 모든 요청 access token 필요")
public class MainController {
    private final MainService mainService;

    @Operation(summary = "메인페이지 계획, 주간 리포트 조회", security = @SecurityRequirement(name= "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "계획 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MainResponse.class),
                    examples = @ExampleObject(
                            name = "MainResponse 예시",
                            summary = "성공 응답 예시",
                            value = """
            {
              "nickname": "테스트 유저",
              "resolution": "오늘도 파이팅!",
              "todayPlans": {
                "planList": [
                  {
                    "planId": 8,
                    "name": "테스트",
                    "memo": "",
                    "categoryName": "공부",
                    "startDate": "2025-08-02",
                    "endDate": "2025-08-07",
                    "minutes": 10,
                    "status": false
                  },
                  {
                    "planId": 10,
                    "name": "테스트",
                    "memo": "",
                    "categoryName": "공부",
                    "startDate": "2025-08-06",
                    "endDate": "2025-08-07",
                    "minutes": 10,
                    "status": false
                  }
                ],
                "achievedPlan": 0,
                "totalPlan": 2,
                "rate": "0.0%",
                "message": "아직 달성한 계획이 없어요. 시작해 볼까요? 😎",
                "totalStudyTime": "00:05:22 (당일 날짜에 걸친 계획 + 타이머의 경과 시간)"
              },
              "weeklyPlans": {
                "planList": [
                  {
                    "planId": 8,
                    "name": "테스트",
                    "memo": "",
                    "categoryName": "공부",
                    "startDate": "2025-08-02",
                    "endDate": "2025-08-07",
                    "minutes": 10,
                    "status": false
                  },
                  {
                    "planId": 9,
                    "name": "삭제 테스트",
                    "memo": null,
                    "categoryName": "공부",
                    "startDate": "2025-08-05",
                    "endDate": "2025-08-05",
                    "minutes": 20,
                    "status": true
                  },
                  {
                    "planId": 10,
                    "name": "테스트",
                    "memo": "",
                    "categoryName": "공부",
                    "startDate": "2025-08-06",
                    "endDate": "2025-08-07",
                    "minutes": 10,
                    "status": false
                  }
                ],
                "achievedPlan": 1,
                "totalPlan": 3,
                "rate": "33.4%",
                "message": "천천히 쌓아가는 중이에요. 남은 기간 동안 더 쌓아봐요! 🏃",
                "totalStudyTime": "14:41:22 (주중 전체 공부 시간 *타이머 경과 시간 포함)"
              }
            }
            """
                    ))),
            @ApiResponse(responseCode = "401", description = "조회 실패",
                content = @Content(mediaType = "application/json",
                schema = @Schema(
                        example = "{\n  \"success\": false,\n  \"message\": \"로그인이 필요한 요청입니다.\"\n}")))
    })
    @GetMapping({"", "/"})
    public ResponseEntity<MainResponse> getMain(@AuthenticationPrincipal CustomUserDetail user){
        return ResponseEntity.ok(mainService.buildMainPage(user.getUser()));
    }
}
