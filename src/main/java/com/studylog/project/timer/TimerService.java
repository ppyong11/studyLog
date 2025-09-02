package com.studylog.project.timer;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.Lap.LapEntity;
import com.studylog.project.Lap.LapRepository;
import com.studylog.project.Lap.LapRequest;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.exception.BadRequestException;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.notification.NotificationEntity;
import com.studylog.project.notification.NotificationRepository;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.sse.SseEmitterService;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final LapRepository lapRepository;
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final SseEmitterService sseEmitterService;
    private final JPAQueryFactory queryFactory;
    private final NotificationRepository notificationRepository;

    private record PlanAndCategory(PlanEntity plan, CategoryEntity category) {} //이 클래스에서만 쓸 거라 static 안 붙임

    //조건 조회
    public List<TimerResponse> searchTimers(UserEntity user, LocalDate startDate, LocalDate endDate,
                                           List<Long> categoryList, Long planId, String keyword, String status, List<String> sort) {
        QTimerEntity timerEntity = QTimerEntity.timerEntity;
        BooleanBuilder builder = new BooleanBuilder();

        List<OrderSpecifier<?>> orders= new ArrayList<>();
        OrderSpecifier<?> dateOrder= null;
        OrderSpecifier<?> categoryOrder= null;

        for(String s:sort){
            String[] arr= s.split(",");
            if(arr.length != 2) throw new BadRequestException("지원하지 않는 정렬입니다.");
            String field= arr[0].trim().toLowerCase();
            String value= arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc")) throw new BadRequestException("지원하지 않는 정렬입니다.");

            switch (field){
                case "date" ->
                    dateOrder= value.equals("desc")? timerEntity.createDate.desc(): timerEntity.createDate.asc();
                case "category" ->
                    categoryOrder= value.equals("desc")? timerEntity.category.name.desc():timerEntity.category.name.asc();
                case "name" ->
                    orders.add(value.equals("desc")? timerEntity.timerName.desc(): timerEntity.timerName.asc());
                default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
        }

        //date, category, name 정렬로 정리
        if(dateOrder != null && categoryOrder != null) {
            orders.add(0, dateOrder);
            orders.add(1, categoryOrder);
        }
        builder.and(timerEntity.user.eq(user));
        if(startDate != null) {
            if (endDate != null) {
                //start~end
                builder.and(timerEntity.createDate.loe(endDate));
            }
            //start~전 일자
            builder.and(timerEntity.createDate.goe(startDate));
        }
        if(planId != null) {
            builder.and(timerEntity.plan.id.eq(planId));
        }

        if(!categoryList.isEmpty()) {
            builder.and(timerEntity.category.id.in(categoryList));
        }
        if(keyword != null && !keyword.isEmpty()) {
            builder.and(timerEntity.timerName.like("%"+keyword+"%"));
        }
        if(status != null){
            TimerStatus timerStatus= TimerStatus.valueOf(status);
            builder.and(timerEntity.status.eq(timerStatus));
        }

        List<TimerEntity> timers= queryFactory.selectFrom(timerEntity)
                .where(builder)
                .orderBy(orders.toArray(new OrderSpecifier[0])) //OrderSpecifier<?> 타입의 배열 반환
                .fetch();
        return timers.stream()
                .map(timer -> TimerResponse.toDto(timer))
                .toList();
    }

    //단일 조회
    public TimerDetailResponse getTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        return TimerDetailResponse.toDto(timer); //여기서 laps도 뽑아옴
    }

    public TimerDetailResponse createTimer(TimerRequest request, UserEntity user) {
        PlanEntity plan= null;
        CategoryEntity category;
        TimerEntity timer;
        if(request.getPlan() != null){
            plan= planRepository.findByUserAndId(user, request.getPlan())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
            checkPlan(plan, null, request);
            category= plan.getCategory();
        } else{ //request에 플랜 X
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        }

        timer= request.toEntity(user, plan, category);
        timerRepository.saveAndFlush(timer); //이때 timer에도 id 매핑됨 (AI 된 값)
        return TimerDetailResponse.toDto(timer); //첫 생성 후 조회
    }

    /*
    원래 타이머의 계획이 없지 않는 이상, request 계획은 항상 들어옴 (수정 X일 경우 내 거로 채워서)
    우선, 내 플랜이 완료처리됨 -> 동일 계획으로 요청들어오면 수정 O
                         -> 다른 계획으로 요청 들어오면 수정 X
    플랜 완료 처리 안됐으면 수정할 계획이 완료 처리 안 됐으면 수정 O
    */
    public TimerDetailResponse updateTimer(Long id, TimerRequest request, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id); //timer 존재 여부
        PlanEntity plan= null;
        CategoryEntity category;

        if(request.getPlan() != null){
            plan= planRepository.findByUserAndId(user, request.getPlan())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 계획입니다."));
            checkPlan(plan, timer, request);
            category= plan.getCategory();
        } else{ //request에 플랜 X
            if(timer.getPlan() != null && timer.getPlan().isStatus())
                throw new BadRequestException("완료 처리된 계획은 수정할 수 없습니다.");
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        }

        timer.updateName(request.getName());
        timer.updatePlan(plan);
        timer.updateCategory(category);
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse startTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        if(timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("이미 실행 중인 타이머입니다.");

        //갱신 안 되게 상태 조건 추가
        if(timerRepository.existsByUserAndStatus(user, TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 있습니다. 정지/종료 후 다시 시도해 주세요.");

        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 타이머는 실행이 불가합니다.");

        timer.start();
        return TimerDetailResponse.toDto(timer);
    }

    //타이머 삭제 + 알림 경로 삭제
    public void deleteTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        //삭제한 타이머의 알림 받기 (영속 상태)
        List<NotificationEntity> notifications= notificationRepository.findAllByUserAndTimer(user, timer);
        for(NotificationEntity noti : notifications){ //해당 타이머를 가진 알림 없으면 패스
            noti.updateTimerId(); //null 처리
            if(!noti.getUrl().equals("/plans")) //타이머 url일 경우 삭제
                noti.updateUrl();
        }

        timerRepository.delete(timer); //랩도 알아서 삭제됨
    }

    //타이머 동기화
    //실행 중인 타이머의 실행 중인 랩 동기화
    public TimerDetailResponse syncedTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(!timer.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 아닙니다.");
        //실행 중인 랩 가져오기
        lapRepository.findByTimerAndStatus(timer, TimerStatus.RUNNING)
                        .ifPresent(lap -> {
                            lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
                            lap.updateSyncedAt();
                        });
        timer.updateElapsed(TimerLapUtils.getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
        timer.updateSyncedAt(); //동기화
        checkCompletion(timer, user, true);
        return TimerDetailResponse.toDto(timer);
    }

    //정지 시 실행 중인 랩도 정지됨
    public TimerDetailResponse pauseTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                timer.pause();
                lapRepository.findByTimerAndStatus(timer, TimerStatus.RUNNING)
                        .ifPresent(lap -> {
                            lap.pause();
                            lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
                        });
            }
            case ENDED -> throw new BadRequestException("종료된 타이머는 정지가 불가합니다.");
            default -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
        }

        timer.updateElapsed(TimerLapUtils.getTotalElapsed(timer)); //누적 시간 갱신
        if(timer.getPlan() != null) checkCompletion(timer, user, false);
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse endTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                timer.end(LocalDateTime.now());
                timer.updateElapsed(TimerLapUtils.getTotalElapsed(timer)); //누적 시간 갱신
            }
            case ENDED -> throw new BadRequestException("이미 종료된 타이머입니다.");
            case READY -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
            case PAUSED -> timer.end(timer.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }
        List<LapEntity> laps= lapRepository.findAllByTimer(timer);
        for (LapEntity lap : laps) {
            switch (lap.getStatus()) {
                case RUNNING -> {
                    lap.end(LocalDateTime.now());
                    lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
                }
                case PAUSED -> {
                    lap.end(lap.getPauseAt()); //pasue -> end는 pauseAt == endAt
                    lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
                }
                case READY -> lap.end(null);
            }
        }
        if(timer.getPlan() != null) checkCompletion(timer, user, false);
        return TimerDetailResponse.toDto(timer);
    }

    //완료 체킹은 그대로
    public TimerDetailResponse resetTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(timer.getPlan().isStatus()) throw new BadRequestException("타이머의 계획이 완료 상태일 경우 초기화가 불가합니다.");
        switch (timer.getStatus()) {
            case ENDED -> throw new BadRequestException("종료된 타이머는 초기화가 불가합니다.");
            case READY -> throw new BadRequestException("초기화 상태입니다.");
            default -> {
                timer.reset();
                List<LapEntity> laps= lapRepository.findAllByTimer(timer);
                for (LapEntity lap : laps) {
                    lap.reset();
                }
            }
        }
        log.info("타이머 초기화 완료");
        return TimerDetailResponse.toDto(timer);
    }
    public TimerDetailResponse startLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapAndValidate(lapId, user, timerId); //연관엔티티 반환
        if(lap.getTimer().getId().equals(timerId))
            throw new BadRequestException("해당 타이머에 존재하지 않는 랩입니다.");
        TimerEntity timer= lap.getTimer();

        if(!lap.getTimer().getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 타이머가 아닙니다.");

        //실행 타이머라면 랩 상태 체크
        if(lap.getStatus().equals(TimerStatus.RUNNING))
            throw new BadRequestException("이미 실행 중인 랩입니다.");

        if(lapRepository.existsByTimerAndStatus(timer, TimerStatus.RUNNING))
            throw new BadRequestException("실행 중인 랩이 있습니다. 정지/종료 후 다시 시도해 주세요.");

        if(lap.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 랩은 재실행이 불가합니다.");

        lap.start();
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse pauseLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapAndValidate(lapId, user, timerId); //연관엔티티 반환
        TimerEntity timer= lap.getTimer();

        switch (lap.getStatus()) {
            case RUNNING -> lap.pause();
            case ENDED -> throw new BadRequestException("종료된 랩은 재실행이 불가합니다.");
            default -> throw new BadRequestException("실행 중인 랩이 아닙니다.");
        }

        lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse endLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapAndValidate(lapId, user, timerId); //연관엔티티 반환
        TimerEntity timer= lap.getTimer();

        switch (lap.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                lap.end(LocalDateTime.now());
                lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap)); //누적 시간 갱신
            }
            case ENDED -> throw new BadRequestException("이미 종료된 랩입니다.");
            case READY -> throw new BadRequestException("실행 중인 랩이 아닙니다.");
            case PAUSED -> lap.end(lap.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }

        return TimerDetailResponse.toDto(timer);
    }
    //랩 추가는 실행, 정지 상관 X, 랩 실행은 무조건 타이머 실행 상태
    public TimerDetailResponse createLap(Long timerId, LapRequest request, UserEntity user) {
        //timer 영속성 컨텍스트에 관리돼서 lap save 하면 컨텍스트에도 새로운 LapEntity 등록됨
        //랩 엔티티 만들 때 타이머만 잘 들어가면 됨
        TimerEntity timer= getTimerByUserAndId(user, timerId);
        if(timer.getStatus().equals(TimerStatus.ENDED))
            throw new BadRequestException("종료된 타이머는 랩 생성이 불가합니다.");
        //타이머에 동일 랩 네임 X
        if(lapRepository.existsByTimerIdAndLapName(timerId, request.getName().trim()))
            throw new BadRequestException("해당 랩명이 존재합니다.");
        //존재 X 랩명이면
        LapEntity lap= request.toEntity(request, timer);
        lapRepository.save(lap); //id 필드도 다 참
        return TimerDetailResponse.toDto(timer);
    }

    public TimerDetailResponse updateLap(Long timerId, Long lapId, LapRequest request, UserEntity user) {
        LapEntity lap= getLapAndValidate(lapId, user, timerId);
        if(lapRepository.existsByTimerIdAndLapName(timerId, request.getName().trim())) {
            if(!lap.getName().equals(request.getName().trim()))
                throw new BadRequestException("해당 랩명이 존재합니다.");
        }
        lap.updateName(request.getName());
        return TimerDetailResponse.toDto(lap.getTimer());
    }

    //타이머 전체 기록에 영향 X
    public TimerDetailResponse deleteLap(Long timerId, Long lapId, UserEntity user) {
        LapEntity lap= getLapAndValidate(lapId, user, timerId);
        lapRepository.delete(lap);
        lapRepository.flush(); //delete 쿼리 안 나가서 넣음
        return TimerDetailResponse.toDto(lap.getTimer());
    }


    public TimerEntity getTimerByUserAndId(UserEntity user, Long id) {
        return timerRepository.findByUserAndId(user, id).orElseThrow(() -> new NotFoundException("존재하지 않는 타이머입니다."));
    }

    private void checkPlan(PlanEntity plan, TimerEntity timer, TimerRequest request) {
        if(timer == null || timer.getPlan() == null || !timer.getPlan().getId().equals(plan.getId())){
            if(timerRepository.existsByPlanId(plan.getId())) throw new BadRequestException("선택한 계획의 타이머가 이미 존재합니다.");
            if(plan.isStatus()) throw new BadRequestException("이미 완료된 계획은 설정할 수 없습니다.");
            if(!plan.getCategory().getId().equals(request.getCategory())){
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
            }
        } else{ //타이머에 plan 있고 수정하려는 plan과 동일할 때
            if(!timer.getPlan().getCategory().getId().equals(request.getCategory()))
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
        }
    }

    private void checkCompletion(TimerEntity timer, UserEntity user, boolean isSyncCheck){
        LocalDate timerStartDate= timer.getSyncedAt() == null? timer.getStartAt().toLocalDate():timer.getSyncedAt().toLocalDate();
        LocalDate planStart= timer.getPlan().getStartDate();
        LocalDate planEnd= timer.getPlan().getEndDate();

        if(timer.getPlan().isStatus() || timer.getPlan().getMinutes() == 0) return; //이미 완료된 계획이거나 목표 시간이 0이라면
        //미완료 & 계획 일자 이후에 수행되었으면

        boolean inRange= false; //체킹 값 저장
        if(!isSyncCheck){ //정지, 종료에 의한 검사
            LocalDate timerEndDate= timer.getEndAt() == null? timer.getPauseAt().toLocalDate():timer.getEndAt().toLocalDate();
            inRange= (timerStartDate.isEqual(planStart) || timerStartDate.isAfter(planStart))
                    && (timerEndDate.isEqual(planEnd) || timerEndDate.isBefore(planEnd));
        } else { //동기화 후 검사
            inRange= (timerStartDate.isEqual(planStart) || timerStartDate.isAfter(planStart))
                    && (timerStartDate.isEqual(planEnd) || timerStartDate.isBefore(planEnd));
        }
        if(inRange && timer.getElapsed() >= timer.getPlan().getMinutes() * 60){
            timer.getPlan().updateStatus(true);
            sseEmitterService.alert(timer, user, isSyncCheck);
        }
    }

    public Optional<TimerEntity> getTimerByPlan(PlanEntity plan){
        return timerRepository.findByPlan(plan);
    }

    public void updateCategory(CategoryEntity deleteCategory, CategoryEntity defaultCategory){
        timerRepository.updateCategory(deleteCategory, defaultCategory);
    }

    @Scheduled(cron= "0 0/5 * * * *") //5분 간격 스케쥴링
    public void updateElapsedSecond() {
        //실행 중인 타이머만 조회
        List<TimerEntity> runningTimerList= timerRepository.findAllByStatus(TimerStatus.RUNNING);
        //timer 영속상태
        for (TimerEntity timer : runningTimerList) {
            timer.updateElapsed(TimerLapUtils.getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
            timer.updateSyncedAt(); //자동 동기화
            if(timer.getPlan() != null){ //타이머에 계획이 있다면
               checkCompletion(timer, timer.getUser(), true);
            }
            lapRepository.findByTimerAndStatus(timer, TimerStatus.RUNNING)
                    .ifPresent(lap -> {
                        lap.updateElapsed(TimerLapUtils.getTotalElapsed(lap));
                        lap.updateSyncedAt();
                    }); //타이머에 실행 중인 랩도 있다면 같이 동기화 처리
        }
    }

    private LapEntity getLapAndValidate(Long lapId, UserEntity user, Long timerId){
        return lapRepository.getLapWithTimer(lapId, user, timerId)
                .orElseThrow(()-> new NotFoundException("해당 타이머에 존재하지 않는 랩입니다."));
    }
}

