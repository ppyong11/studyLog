package com.studylog.project.timer;

import com.studylog.project.global.CommonUtil;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.user.UserEntity;
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
    @GetMapping("{timerId}")
    public ResponseEntity<TimerDetailResponse> getTimer(@PathVariable("timerId") Long id,
                                                        @AuthenticationPrincipal UserEntity user) {
        TimerDetailResponse response = timerService.getTimer(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<TimerDetailResponse> createTimer(@Valid @RequestBody TimerRequest request,
                                                           @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.createTimer(request, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}/start")
    public ResponseEntity<TimerDetailResponse> startTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.startTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}/pause")
    public ResponseEntity<TimerDetailResponse> pauseTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.pauseTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}/end")
    public ResponseEntity<TimerDetailResponse> endTimer(@PathVariable("timerId") Long id,
                                                        @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.endTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{timerId}")
    public ResponseEntity<TimerDetailResponse> updateTimer(@PathVariable("timerId") Long id,
                                                           @Valid @RequestBody TimerRequest request,
                                                           @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.updateTimer(id, request,user.getUser());
        return ResponseEntity.ok(response);
    }

    //경과 시간 리셋
    @PatchMapping("{timerId}/reset")
    public ResponseEntity<TimerDetailResponse> resetTimer(@PathVariable("timerId") Long id,
                                                          @AuthenticationPrincipal CustomUserDetail user) {
        TimerDetailResponse response= timerService.resetTimer(id, user.getUser());
        return ResponseEntity.ok(response);
    }

    //타이머 계획 or 카테고리 업데이트 시, 계획/카테고리 대조 잘하기 계획 잇는데 카테고리 다른 거로 바꿀 수 X
    @DeleteMapping("/{timerId}")
    public ResponseEntity<ApiResponse> deleteTimer(@PathVariable("timerId") Long id,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        timerService.deleteTimer(id, user.getUser());
        return ResponseEntity.ok(new ApiResponse( true, "타이머가 삭제되었습니다."));
    }

    //동기화 컨트롤러
    @PatchMapping("{timerId}/sync")
    public ResponseEntity<TimerDetailResponse> syncedTimer(@PathVariable("timerId") Long id,
                                                   @AuthenticationPrincipal CustomUserDetail user) {
        return ResponseEntity.ok(timerService.syncedTimer(id, user.getUser()));
    }
}
