package com.studylog.project.notification;

import com.studylog.project.global.exception.CustomException;
import com.studylog.project.global.exception.ErrorCode;
import com.studylog.project.global.response.ScrollResponse;
import com.studylog.project.global.response.SuccessResponse;
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
    private final NotificationRepositotyImpl notificationRepositotyImpl;

    public ScrollResponse<NotificationResponse> getAllNoti(int page, UserEntity user){

        long pageSize= 10;

        //엔티티 조회 + response로 매핑 (생성자에 들어감)
        List<NotificationResponse> responses = notificationRepositotyImpl.findAllNotifications(user, page);

        Long totalItems = notificationRepositotyImpl.totalItems(user);

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
                timer.getPlan().getName(), isSyncCheck ? "자동" : "");
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .timer(timer) //null이면 알아서 들어감
                .title(title)
                .content(isSyncCheck ? "해당 타이머로 이동해서 타이머를 종료해 주세요." : "확인하러 가볼까요?")
                .url(isSyncCheck ? "timers/" + timer.getId() : "plans/" + timer.getPlan().getId())
                .build();
        notificationRepository.save(notification);
    }

    public SuccessResponse<Void> deleteAllNoti(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllByUser(user);
        if(notifications.isEmpty()) return SuccessResponse.of( "삭제할 알림이 없습니다.");
        notificationRepository.deleteAll(notifications); //인자 없으면 모든 행 삭제
        return SuccessResponse.of( "모든 알림이 삭제되었습니다.");
    }
    public void deleteNoti(Long id, UserEntity user){
        NotificationEntity notification= notificationRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new CustomException(ErrorCode.NOTI_NOT_FOUND));
        notificationRepository.delete(notification);
    }

    public void readNoti(Long id, UserEntity user){
        NotificationEntity notification= notificationRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new CustomException(ErrorCode.NOTI_NOT_FOUND));
        notification.updateIsRead();
    }

    public SuccessResponse<Void> readAllNoti(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllByUserAndIsReadFalse(user);
        if(notifications.isEmpty()) {
            return SuccessResponse.of("읽음 처리할 알림이 없습니다.");
        }
        for(NotificationEntity noti:notifications) {
            noti.updateIsRead();
        }
        return SuccessResponse.of("모든 알림이 읽음 처리되었습니다.");
    }

}
