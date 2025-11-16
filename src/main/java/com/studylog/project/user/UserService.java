package com.studylog.project.user;

import com.studylog.project.board.BoardRepository;
import com.studylog.project.category.CategoryRepository;
import com.studylog.project.category.CategoryService;
import com.studylog.project.global.exception.*;
import com.studylog.project.jwt.CustomUserDetail;
import com.studylog.project.notification.NotificationService;
import com.studylog.project.plan.PlanRepository;
import com.studylog.project.timer.TimerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; //제네릭 타입 명시
    private final CategoryService categoryService;
    private final NotificationService notificationService;

    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;
    private final BoardRepository boardRepository;
    private final TimerRepository timerRepository;

    @Transactional
    //회원 DB에 저장
    public void register(SignUpRequest request) {
        existsId(request.id());
        existsNickname(request.nickname());
        existsEmail(request.email());

        // null안정 보장
        if(!Boolean.TRUE.equals(redisTemplate.hasKey("verified:" + request.email())))
        {// 키 없는 경우
            throw new CustomException(ErrorCode.AUTH_SESSION_EXPIRED);
        }
        String encryptedPw= passwordEncoder.encode(request.password());
        UserEntity userEntity = request.toEntity();
        userEntity.setEncodedPw(encryptedPw); //빌더 객체 pw 값 바뀜
        userRepository.save(userEntity);
        userRepository.flush(); //userEntity DB에 저장 후 카테고리 넣기
        //위에 거 안 하면 메서드 다 끝나고 user 테이블에 저장해서 아래 코드 오류남 (엔티티 X)
        log.info("{}", userEntity.getUser_id());
        categoryService.defaultCategory(userEntity);
    }

    @Transactional
    //아이디, 비밀번호 확인
    public void validateAndRestoreUser(LogInRequest request) {
        //아이디 검증
        UserEntity userEntity= userRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAIL));

        if (passwordEncoder.matches(request.pw(), userEntity.getPw())) {

            log.info(String.format("id: [%s], 로그인 성공", userEntity.getId()));
            if(userEntity.isDelete()){//회원탈퇴한 회원이라면
                //7일 지났는데 스케쥴러 안 돌아서 삭제 안 된 경우
                if(userEntity.getDeleteAt().isBefore(LocalDateTime.now().minusDays(7))){
                    throw new CustomException(ErrorCode.USER_DELETED);
                }
                restore(userEntity); //복구 처리
            }
        } else{
            log.warn(String.format("id: [%s], 로그인 실패", userEntity.getId()));
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }
    }

    @Transactional
    //비밀번호 변경
    public void changePw(UserEntity user, UpdatePwRequest request) { //암호화된 비번(DTO), 평문 비번(request)
        String currentPw = request.currentPw();
        String newPw = request.newPw();

        UserEntity userEntity = getUser(user, ErrorCode.USER_NOT_FOUND); // principal의 user 객체를 entity에 넣음

        if (!passwordEncoder.matches(currentPw, user.getPw())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (currentPw.equals(newPw)) {
            throw new CustomException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String encryptedPw = passwordEncoder.encode(newPw);

        userEntity.changePw(encryptedPw);
        log.info("changePw: 비밀번호 변경 완료");
    }

    @Transactional
    //닉네임 변경
    public void changeNickname(UserEntity user, String newNickname) {
        UserEntity userEntity = getUser(user, ErrorCode.USER_NOT_FOUND); // 요청 날린 토큰의 인증 객체로 영속성 컨텍스트 저장

        if (userEntity.getNickname().equals(newNickname)) {
            // 닉네임이 동일하면 아무 작업 X
            return;
        }

        existsNickname(newNickname);

        userEntity.changeNickname(newNickname); // 더티 체킹
        //변경 값 감지 후 update
    }

    //회원 탈퇴 취소
    public void restore(UserEntity user){
        //로그인한 회원이 회원탈퇴한 회원이라면, 탈퇴 취소
        user.restore(); //is_delete= false, deleteAt= null
        log.info("탈퇴 철회 완료: 탈퇴 여부 {}, 시간 {}, ", user.isDelete(), user.getDeleteAt());
    }

    //회원탈퇴
    @Transactional
    public void withdraw(UserEntity user){
        UserEntity userEntity= getUser(user, ErrorCode.USER_NOT_FOUND);
        userEntity.withdraw(LocalDateTime.now()); //기존 객체를 바꾸는 거니까 빌더 필요 X
        log.info("탈퇴 처리 완료: 여부 {}, 시간 {}, ", userEntity.isDelete(), userEntity.getDeleteAt());
    }

    public void updateResolution(String resolution, UserEntity user){
        UserEntity userEntity= getUser(user, ErrorCode.USER_NOT_FOUND); //영속 상태 만들기
        userEntity.updateResolution(resolution);
    }

    public UserInfoResponse getUserInfo(UserEntity user){
        return new UserInfoResponse(user.getNickname(), user.getResolution(),
                notificationService.getUnreadCount(user));
    }

    public UserEntity getUser(UserEntity user, ErrorCode errorCode){
        return userRepository.findById(user.getUser_id())
                .orElseThrow(() -> new CustomException(errorCode)); //회원 객체 받기
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) //서버 실행 시간부터 30분마다 실행
    @Transactional
    public void deleteUser(){
        //isDelete == true && deleteAt < 지금 일자 - 3일 튜플 찾음
        List<UserEntity> users = userRepository.findAllByDeleteTrueAndDeleteAtBefore(LocalDateTime.now().minusDays(7));
        for(UserEntity user : users){
            //카테고리 먼저 삭제하면 카테고리를 가진 테이블에 제약사항 에러 터짐
            boardRepository.deleteAllByUser(user);
            timerRepository.deleteAllByUser(user);
            planRepository.deleteAllByUser(user);
            categoryRepository.deleteAllByUser(user);
            userRepository.delete(user);
        }
    }

    //컨트롤러에서도 repo 접근해야 돼서 만듦... 이왕 만든 김에 서비스에서도 그냥 사용
    //서비스에서는 레포로 바로 접근해도 문제 X, 컨트롤러->서비스->레포
    public void existsEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.MAIL_DUPLICATE);
        }
    }
    public void existsId(String id) {
        if (userRepository.existsById(id)) {
            throw new CustomException(ErrorCode.ID_DUPLICATE);
        }
    }
    public void existsNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
        }
    }

    public UserResponse getCurrentUser(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.of(userEntity);
    }


}
