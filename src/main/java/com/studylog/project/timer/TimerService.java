package com.studylog.project.timer;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.global.PageResponse;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final SseEmitterService sseEmitterService;
    private final JPAQueryFactory queryFactory;
    private final NotificationRepository notificationRepository;
    //조건 조회
    public PageResponse<TimerResponse> searchTimers(UserEntity user, LocalDate startDate, LocalDate endDate,
                                                    List<Long> categoryList, String planKeyword, String keyword, String status, List<String> sort,
                                                    int page) {
        QTimerEntity timerEntity = QTimerEntity.timerEntity;
        BooleanBuilder builder = new BooleanBuilder();

        OrderSpecifier<?>[] orders= new OrderSpecifier[3];


        for(String s:sort){
            String[] arr= s.split(",");
            if(arr.length != 2) throw new BadRequestException("지원하지 않는 정렬입니다.");
            String field= arr[0].trim().toLowerCase();
            String value= arr[1].trim().toLowerCase();
            if(!value.equals("asc") && !value.equals("desc")) throw new BadRequestException("지원하지 않는 정렬입니다.");

            switch (field){
                case "date" ->
                    orders[0]= value.equals("desc")? timerEntity.createAt.desc(): timerEntity.createAt.asc();
                case "category" ->
                    orders[1]= value.equals("desc")? timerEntity.category.name.desc():timerEntity.category.name.asc();
                case "name" ->
                    orders[2]= value.equals("desc")? timerEntity.name.desc(): timerEntity.name.asc();
                default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
            }
        }

        if(orders[0] == null || orders[1] == null || orders[2] == null)
            throw new BadRequestException("지원하지 않는 정렬입니다.");

        builder.and(timerEntity.user.eq(user));

        if(startDate != null) { //startDate 있으면 endDate 있음 (컨트롤러에서 검증)
            //Date to DateTime (MIN: 00:00:00, MAX: 23:59:59)
            builder.and(timerEntity.createAt.goe(startDate.atTime(LocalTime.MIN))); // >=
            builder.and(timerEntity.createAt.loe(endDate.atTime(LocalTime.MAX))); // <=
        }

        if(planKeyword != null && !planKeyword.isEmpty()) {
            builder.and(timerEntity.plan.name.like('%' + planKeyword + '%'));
        }

        if(!categoryList.isEmpty()) {
            builder.and(timerEntity.category.id.in(categoryList));
        }
        //조인 안 하면 암묵적 접근으로 알아서 join or 서브쿼리로 나감
        if(keyword != null && !keyword.isEmpty()) {
            builder.and(timerEntity.name.like("%"+keyword+"%"));
        }
        if(status != null){
            TimerStatus timerStatus= TimerStatus.valueOf(status);
            builder.and(timerEntity.status.eq(timerStatus));
        }

        long pageSize= 20;
        long offset= (page - 1) * pageSize;

       List<TimerResponse> timerResponses= queryFactory
                .select(Projections.constructor(
                        TimerResponse.class,
                        timerEntity.id,
                        timerEntity.name,
                        timerEntity.plan.name, //외래키로 연결된 다른 테이블 컬럼값 가져오려면 join 필수
                        Expressions.stringTemplate(
                                "'plans/' || {0}", timerEntity.plan.id //페이지 URL
                        ),
                        timerEntity.category.id, //Projections이라 join 영속성 관리 안 됨 -> 바로 DTO 매핑이기 때문
                        timerEntity.createAt,
                        timerEntity.elapsed,
                        timerEntity.status
                ))
                .from(timerEntity)
                .leftJoin(timerEntity.plan) //select(엔티티) 아니라서 fetchJoin 쓰면 오류남
                .leftJoin(timerEntity.category)
                .where(builder)
                .orderBy(orders)
                .offset(offset)
                .limit(pageSize)
                .fetch();

        Long totalItems= queryFactory
                .select(timerEntity.count())
                .from(timerEntity)
                .where(builder)
                .fetchOne();

        long totalPages= (totalItems + pageSize - 1) / pageSize;
        return new PageResponse<>(timerResponses, totalItems, totalPages, page, pageSize);
    }

    //단일 조회
    public TimerDetailResponse getTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
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
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed())); //첫 생성 후 조회
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
            if(timer.getPlan() != null && timer.getPlan().isComplete())
                throw new BadRequestException("완료 처리된 계획은 수정할 수 없습니다.");
            category= categoryRepository.findByUserAndId(user, request.getCategory())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
        }

        timer.updateName(request.getName());
        timer.updatePlan(plan);
        timer.updateCategory(category);
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
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
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
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
        timer.updateElapsed(getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
        timer.updateSyncedAt(); //동기화
        checkCompletion(timer, user, true);
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
    }

    //정지 시 실행 중인 랩도 정지됨
    public TimerDetailResponse pauseTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> timer.pause();
            case ENDED -> throw new BadRequestException("종료된 타이머는 정지가 불가합니다.");
            default -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
        }

        timer.updateElapsed(getTotalElapsed(timer)); //누적 시간 갱신
        if(timer.getPlan() != null) checkCompletion(timer, user, false);
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
    }

    public TimerDetailResponse endTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);

        switch (timer.getStatus()) { //디폴트 안 써도 됨
            case RUNNING -> {
                timer.end(LocalDateTime.now());
                timer.updateElapsed(getTotalElapsed(timer)); //누적 시간 갱신
            }
            case ENDED -> throw new BadRequestException("이미 종료된 타이머입니다.");
            case READY -> throw new BadRequestException("실행 중인 타이머가 아닙니다.");
            case PAUSED -> timer.end(timer.getPauseAt()); //정지된 타이머라면 정지 시간 == 종료 시간 (누적 시간 갱신은 정지할 때 함)
        }
        if(timer.getPlan() != null) checkCompletion(timer, user, false);
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
    }

    //완료 체킹은 그대로
    public TimerDetailResponse resetTimer(Long id, UserEntity user) {
        TimerEntity timer= getTimerByUserAndId(user, id);
        if(timer.getPlan().isComplete()) throw new BadRequestException("타이머의 계획이 완료 상태일 경우 초기화가 불가합니다.");
        switch (timer.getStatus()) {
            case ENDED -> throw new BadRequestException("종료된 타이머는 초기화가 불가합니다.");
            case READY -> throw new BadRequestException("초기화 상태입니다.");
            default -> timer.reset();
        }
        return TimerDetailResponse.toDto(timer, formatElapsed(timer.getElapsed()));
    }

    public TimerEntity getTimerByUserAndId(UserEntity user, Long id) {
        return timerRepository.getTimerWithPlanCategory(user, id).orElseThrow(() -> new NotFoundException("존재하지 않는 타이머입니다."));
    }

    private void checkPlan(PlanEntity plan, TimerEntity timer, TimerRequest request) {
        if(timer == null || timer.getPlan() == null || !timer.getPlan().getId().equals(plan.getId())){
            if(timerRepository.existsByPlanId(plan.getId())) throw new BadRequestException("선택한 계획의 타이머가 이미 존재합니다.");
            if(plan.isComplete()) throw new BadRequestException("이미 완료된 계획은 설정할 수 없습니다.");
            if(!plan.getCategory().getId().equals(request.getCategory())){
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
            }
        } else{ //타이머에 plan 있고 수정하려는 plan과 동일할 때
            if(!timer.getPlan().getCategory().getId().equals(request.getCategory()))
                throw new BadRequestException("입력된 카테고리가 계획 카테고리와 일치하지 않습니다.");
        }
    }

    public long getTotalElapsed(TimerEntity entity) {
        LocalDateTime time= null;
        LocalDateTime startAt;

        if(entity.getSyncedAt() == null){ //동기화 전
            startAt = entity.getStartAt();
            switch (entity.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= entity.getPauseAt();
                case ENDED -> time= entity.getEndAt();
            }
        } else {
            startAt = entity.getSyncedAt();
            switch (entity.getStatus()) { //디폴트 안 써도 됨
                case RUNNING -> time = LocalDateTime.now();
                case PAUSED -> time= entity.getPauseAt();
                case ENDED -> time= entity.getEndAt();
            }
        }

        Duration duration= Duration.between(startAt, time);
        return duration.getSeconds() + entity.getElapsed();
    }

    public String formatElapsed(long elapsedSeconds){
        long hours= elapsedSeconds / 3600;
        long minutes= (elapsedSeconds % 3600) / 60;
        long seconds= elapsedSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void checkCompletion(TimerEntity timer, UserEntity user, boolean isSyncCheck){
        LocalDate timerStartDate= timer.getSyncedAt() == null? timer.getStartAt().toLocalDate():timer.getSyncedAt().toLocalDate();
        LocalDate planStart= timer.getPlan().getStartDate();
        LocalDate planEnd= timer.getPlan().getEndDate();

        if(timer.getPlan().isComplete() || timer.getPlan().getMinutes() == 0) return; //이미 완료된 계획이거나 목표 시간이 0이라면
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
            timer.updateElapsed(getTotalElapsed(timer)); //누적 경과+startAt+동기화 시간
            timer.updateSyncedAt(); //자동 동기화
            if(timer.getPlan() != null){ //타이머에 계획이 있다면
               checkCompletion(timer, timer.getUser(), true);
            }
        }
    }

}

