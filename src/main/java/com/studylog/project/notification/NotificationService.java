package com.studylog.project.notification;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studylog.project.global.ScrollResponse;
import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.global.response.CommonResponse;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JPAQueryFactory queryFactory;

    public ScrollResponse<NotificationResponse> getAllNoti(int page, UserEntity user){
        QNotificationEntity notiEntity= QNotificationEntity.notificationEntity;
        BooleanBuilder builder= new BooleanBuilder();

        builder.and(notiEntity.user.eq(user));

        long pageSize= 10;
        long offset= (page - 1) * pageSize;

        //엔티티 조회 + response로 매핑 (생성자에 들어감)
        List<NotificationResponse> responses= queryFactory
                .select(Projections.constructor(
                        NotificationResponse.class,
                        notiEntity.id,
                        notiEntity.title,
                        notiEntity.content,
                        notiEntity.alertAt,
                        notiEntity.url,
                        notiEntity.isRead
                ))
                .from(notiEntity)
                .where(builder)
                .offset(offset)
                .limit(pageSize)
                .fetch();

        Long totalItems= queryFactory
                .select(notiEntity.count())
                .from(notiEntity)
                .where(builder)
                .fetchOne();
        log.info("{}", totalItems);
        boolean hasNext= page * pageSize < totalItems;
        return new ScrollResponse<>(responses, totalItems, page, pageSize, hasNext);
    }

    public long getUnreadCount(UserEntity user){
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    public void saveNotification(UserEntity user, TimerEntity timer, boolean isSyncCheck) {
        //DB에 알림 저장
        //동기화로 완료 체크 시 - 타이머 있음
        //정지, 종료로 체크 시 - 타이머 없음
        String title = String.format("[%s] 계획이 %s완료 처리되었어요. 🥳",
                timer.getPlan().getPlan_name(), isSyncCheck ? "자동" : "");
        String content = "해당 타이머로 이동해서 타이머를 종료해 주세요.";
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .timer(timer) //null이면 알아서 들어감
                .title(title)
                .content(isSyncCheck ? content : "다른 목표도 달성하러 가볼까요?")
                .url(isSyncCheck ? "timers/" + timer.getId() : "plans/" + timer.getPlan().getId())
                .build();
        notificationRepository.save(notification);
    }

    public CommonResponse deleteAllNoti(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllByUser(user);
        if(notifications.isEmpty()) return new CommonResponse(true, "삭제할 알림이 없습니다.");
        notificationRepository.deleteAll(notifications); //인자 없으면 모든 행 삭제
        return new CommonResponse(true, "모든 알림이 삭제되었습니다.");
    }
    public void deleteNoti(Long id, UserEntity user){
        NotificationEntity notification= notificationRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new NotFoundException("존재하지 않는 알림입니다."));
        notificationRepository.delete(notification);
    }

    public void readNoti(Long id, UserEntity user){
        NotificationEntity notification= notificationRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new NotFoundException("존재하지 않는 알림입니다."));
        notification.updateIsRead();
    }

    public CommonResponse readAllNoti(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllByUserAndIsReadFalse(user);
        if(notifications.isEmpty()) return new CommonResponse(true, "읽음 처리할 알림이 없습니다.");
        for(NotificationEntity noti:notifications) {
            noti.updateIsRead();
        }
        return new CommonResponse(true, "모든 알림이 읽음 처리되었습니다.");
    }

}
