package com.studylog.project.notification;

import com.studylog.project.global.exception.NotFoundException;
import com.studylog.project.global.response.ApiResponse;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.user.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotifications(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllbyUser(user);
        return notifications.stream()
                .map(notification -> NotificationResponse.toDto(notification))
                .toList();
    }
    public void saveNotification(UserEntity user, TimerEntity timer, boolean isSyncCheck) {
        //DB에 알림 저장
        //동기화로 완료 체크 시 - 타이머 있음
        //정지, 종료로 체크 시 - 타이머 없음
        String title = String.format("[%s] 계획이 목표 달성 시간을 채워 %s완료 처리되었어요. 🥳",
                timer.getPlan(), isSyncCheck ? "자동" : "");
        String content = String.format("[%s]로 이동해서 타이머를 종료해 주세요.", timer.getTimerName());
        String timerUrl = String.format("/timers/%d", timer.getId());
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .timer(timer) //null이면 알아서 들어감
                .title(title)
                .content(isSyncCheck ? content : "알림을 클릭하면 계획 페이지로 이동돼요.")
                .url(isSyncCheck ? timerUrl : "/plans")
                .build();
        notificationRepository.save(notification);
    }

    public ApiResponse deleteAllNotification(UserEntity user){
        List<NotificationEntity> notifications= notificationRepository.findAllbyUser(user);
        if(notifications.isEmpty()) return new ApiResponse(true, "삭제할 알림이 없습니다.");
        notificationRepository.deleteAll(notifications); //인자 없으면 모든 행 삭제
        return new ApiResponse(true, "모든 알림이 삭제되었습니다.");
    }
    public void deleteNotification(Long id, UserEntity user){
        NotificationEntity notification= notificationRepository.findByUserAndId(user, id)
                .orElseThrow(()-> new NotFoundException("존재하지 않는 알림입니다."));
        notificationRepository.delete(notification);
    }
}
