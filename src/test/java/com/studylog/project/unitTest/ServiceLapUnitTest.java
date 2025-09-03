package com.studylog.project.unitTest;

import com.studylog.project.Lap.LapEntity;
import com.studylog.project.Lap.LapRepository;
import com.studylog.project.Lap.LapRequest;
import com.studylog.project.category.CategoryEntity;
import com.studylog.project.plan.PlanEntity;
import com.studylog.project.timer.TimerDetailResponse;
import com.studylog.project.timer.TimerEntity;
import com.studylog.project.timer.TimerRepository;
import com.studylog.project.timer.TimerService;
import com.studylog.project.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceLapUnitTest {
    @Mock
    TimerRepository timerRepository;
    @Mock
    LapRepository lapRepository;
    @InjectMocks
    TimerService timerService;

    @Test
    void 랩_추가_성공() throws Exception {
        //given
        UserEntity user= new UserEntity(); //서비스에서 아무 필드 안 써서 필드 안넣음
        PlanEntity plan= new PlanEntity();
        CategoryEntity category= new CategoryEntity();
        TimerEntity timer= new TimerEntity("timer1", user, plan, category);
        LapRequest request= new LapRequest("랩1");

        //getTimerByUserAndId mock으로 만들기
        TimerService spyService= Mockito.spy(timerService);
        doReturn(timer).when(spyService).getTimerByUserAndId(user, 1L);

        when(lapRepository.existsByTimerIdAndName(1L, "랩1")).thenReturn(false);
        when(lapRepository.save(any(LapEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //when
        TimerDetailResponse response= spyService.createLap(1L, request, user);
    }
}
