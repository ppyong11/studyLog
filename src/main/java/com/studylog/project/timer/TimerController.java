package com.studylog.project.timer;

import com.studylog.project.Lap.LapResponse;
import com.studylog.project.board.BoardDetailResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/timers")
@Tag(name="Timer", description = "Timer API, 모든 요청 access token 필요")
public class TimerController {
    private final TimerService timerService;
    private static final Set<String> VALID_STATUS= Set.of(
            "READY", "RUNNING", "PAUSED", "ENDED"
    );

    //타이머 검색
    /* 정렬은 생성일(내림차)-카테고리-이름순(오름차)
       1. 키워드 검색
       2. 카테고리 검색
       3. 생성일 검색 (start~end)
       4. 계획으로 검색
       5. 상태로 검색
    */
    @Operation(summary = "타이머 목록 조회 (리스트)", description = "정렬(sort) 기본 값: 생성일 내림차순 + 카테고리명/타이머명 오름차순")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = TimerResponse.class))))
    })
    @GetMapping("/search")
    public ResponseEntity<List<TimerResponse>> searchTimer(@RequestParam(required = false) LocalDate startDate,
                                                     @RequestParam(required = false) LocalDate endDate,
                                                     @RequestParam(required = false) String category,
                                                     @RequestParam(required = false) Long plan,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) List<String> sort,
                                                     @AuthenticationPrincipal CustomUserDetail user) {
        List<Long> categoryList= new ArrayList<>();
        status= status == null? null:status.trim().toUpperCase();

        if (sort == null || sort.isEmpty()) sort= List.of("date,desc", "category,asc", "name,asc");
        if (endDate != null && startDate == null) throw new BadRequestException("시작 일자는 필수 입력 값입니다.");
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException("시작 날짜가 종료 날짜보다 뒤일 수 없습니다.");
            }
        }
        if (category != null && !category.trim().isEmpty()){
            categoryList= CommonUtil.parseAndValidateCategory(category);
        }
        if (status != null && !VALID_STATUS.contains(status)) { //null 들어가도 문제 X
            throw new BadRequestException("입력한 상태값이 올바르지 않습니다.");
        }
        keyword= keyword == null ? null : keyword.trim();
        List<TimerResponse> timerList= timerService.searchTimers(user.getUser(), startDate, endDate, categoryList,
                plan, keyword, status, sort);
        return ResponseEntity.ok(timerList);
    }

    //단일 조회
    @Operation(summary = "타이머 단일 조회 (상세 조회)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content= @Content(mediaType = "application/json",
            schema = @Schema(implementation = TimerDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "조회 실패",
            content = @Content(mediaType = "application/json",
            schema = @Schema(
                    example = "{\n  \"success\": false,\n  \"message\": \"존재하지 않는 타이머입니다.\"\n}")))
    })
    @GetMapping("/{timerId}")
    public ResponseEntity<TimerDetailResponse> getTimer(@PathVariable("timerId") Long id,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response = timerService.getTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타이머 등록", description = "카테고리 필수, 계획 선택 / 계획 입력 시 계획의 카테고리와 동일해야 함")
    @PostMapping("")
    public ResponseEntity<TimerDetailResponse> createTimer(@Valid @RequestBody TimerRequest request,
                                                           @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.createTimer(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타이머 시작")
    @PatchMapping("/{timerId}/start")
    public ResponseEntity<TimerDetailResponse> startTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.startTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타이머 정지 (실행 중인 랩 함께 정지)")
    @PatchMapping("/{timerId}/pause")
    public ResponseEntity<TimerDetailResponse> pauseTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.pauseTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타이머 종료 (타이머에 포함된 모든 랩 함께 종료)")
    @PatchMapping("/{timerId}/end")
    public ResponseEntity<TimerDetailResponse> endTimer(@PathVariable("timerId") Long id,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.endTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "타이머 수정", description = "설정된 계획이 완료 상태라면, 타이머 계획 수정 불가")
    @PatchMapping("/{timerId}")
    public ResponseEntity<TimerDetailResponse> updateTimer(@PathVariable("timerId") Long id,
                                                           @Valid @RequestBody TimerRequest request,
                                                           @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.updateTimer(id, request,user.getUser());
        return ResponseEntity.ok(response);
    }

    //경과 시간 리셋
    @Operation(summary = "타이머 초기화", description = "이미 종료된 타이머거나 계획이 완료된 경우 초기화 불가")
    @PatchMapping("{timerId}/reset")
    public ResponseEntity<TimerDetailResponse> resetTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.resetTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    //타이머 계획 or 카테고리 업데이트 시, 계획/카테고리 대조 잘하기 계획 잇는데 카테고리 다른 거로 바꿀 수 X
    @Operation(summary = "타이머 삭제 (타이머 랩 함께 삭제)")
    @DeleteMapping("/{timerId}")
    public ResponseEntity<CommonResponse> deleteTimer(@PathVariable("timerId") Long id,
                                                      @AuthenticationPrincipal CustomUserDetail user) {
        timerService.deleteTimer(id, user.getUser());
        return ResponseEntity.ok(new CommonResponse( true, "타이머가 삭제되었습니다."));
    }

    //동기화 컨트롤러
    @Operation(summary = "타이머 수동 동기화", description = "타이머 경과 시간 갱신, 계획 자동 완료 처리 (sse 알림)")
    @PatchMapping("{timerId}/sync")
    public ResponseEntity<TimerDetailResponse> syncedTimer(@PathVariable("timerId") Long id,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(timerService.syncedTimer(id, user.getUser()));
    }
}
