package com.studylog.project.user;

import com.studylog.project.category.CategoryService;
import com.studylog.project.global.exception.*;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; //제네릭 타입 명시
    private final CategoryService categoryService;

    /* @RequiredArgsConstructor 사용으로 생략
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }*/

    @Transactional
    //회원 DB에 저장
    public void register(SignInRequest user) {
        if(existsId(user.getId())){
            throw new DuplicateException(String.format("[%s] 이미 가입된 회원입니다.", user.getId()));
        }
        if(existsNickname(user.getNickname())){
            throw new DuplicateException(String.format("[%s] 이미 가입된 회원입니다.", user.getNickname()));
        }
        if(existsEmail(user.getEmail())){
            throw new DuplicateException(String.format("[%s] 이미 가입된 회원입니다.", user.getEmail()));
        }
        if(!Boolean.TRUE.equals(redisTemplate.hasKey("verified:" + user.getEmail())))
        {//키 없는 경우
            throw new MailException("인증 세션이 만료됐거나 인증된 메일이 아닙니다.");
        }
        String encryptedPw= passwordEncoder.encode(user.getPw());
        UserEntity userEntity = user.toEntity();
        userEntity.setEncodedPw(encryptedPw); //빌더 객체 pw 값 바뀜
        userRepository.save(userEntity);
        userRepository.flush(); //userEntity DB에 저장 후 카테고리 넣기
        //위에 거 안 하면 메서드 다 끝나고 user 테이블에 저장해서 아래 코드 오류남 (엔티티 X)
        log.info("{}", userEntity.getUser_id());
        categoryService.defaultCategory(userEntity);
    }
    //로그인한 유저 닉네임 반환
    public String getNickname(LogInRequest request){
        UserEntity userEntity= userRepository.findById(request.getId())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("[%s]에 해당하는 회원을 찾을 수 없습니다.", request.getId())));
        return userEntity.getNickname();
    }

    @Transactional
    //아이디, 비밀번호 확인
    public void validateAndRestoreUser(LogInRequest request) {
        //아이디 검증
        UserEntity userEntity= userRepository.findById(request.getId())
                .orElseThrow(() -> new LoginFaildException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (passwordEncoder.matches(request.getPw(), userEntity.getPw())) {
            log.info(String.format("id: [%s], 로그인 성공", userEntity.getId()));
            if(userEntity.isDelete()){//회원탈퇴한 회원이라면
                //3일 지났는데 스케쥴러 안 돌아서 삭제 안 된 경우
                if(userEntity.getDeleteAt().isBefore(LocalDateTime.now().minusMinutes(3))){
                    throw new AlreadyDeleteUserException("회원 탈퇴 철회 기간이 지나 복구가 불가합니다." +
                            "");
                }
                restore(userEntity); //복구 처리
            }
        } else{
            log.warn(String.format("id: [%s], 로그인 실패", userEntity.getId()));
            throw new LoginFaildException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    //비밀번호 변경
    public void changePw(CustomUserDetail customUserDTO, UpdatePwRequest pwRequest) { //암호화된 비번(DTO), 평문 비번(request)
        if (passwordEncoder.matches(pwRequest.getCurrentPw(), customUserDTO.getPassword())){
            //평문 비번과 암호화 비번이 동일하다면
            if(pwRequest.getCurrentPw().equals(pwRequest.getNewPw())){
                throw new InvalidRequestException("기존 비밀번호와 새로운 비밀번호가 일치합니다.");
            }
            String encryptedPw= passwordEncoder.encode(pwRequest.getNewPw()); //새 비번 암호화
            UserEntity userEntity= userRepository.findById(customUserDTO.getUser().getUser_id())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다.")); //principal의 user 객체를 entity에 넣음
            userEntity.changePw(encryptedPw);
            log.info("changePw: 비밀번호 변경 완료");
        }
        else{ //현재 비번 일치 X
            log.info("changePw: 비밀번호 변경 실패");
            throw new InvalidRequestException("기존 비밀번호와 일치하지 않습니다.");
        }
    }

    @Transactional
    //닉네임 변경
    public void changeNickname(CustomUserDetail customUserDTO,UpdateNicknameRequest nicknameRequest) {
        String currentNick= customUserDTO.getUser().getNickname();
        if(currentNick.equals(nicknameRequest.getNickname())){
            //기존 닉네임 동일 시
            throw new InvalidRequestException("현재 닉네임과 동일합니다.");
        }
        //닉네임 중복 시 (존재 닉네임)
        if(existsNickname(nicknameRequest.getNickname())){
            throw new DuplicateException("이미 존재하는 닉네임입니다.");
        }
        //위 경우가 아니라면
        UserEntity userEntity= userRepository.findById(customUserDTO.getUser().getUser_id())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다.")); //요청 날린 토큰의 인증 객체로 영속성 컨텍스트 저장
        userEntity.changeNickname(nicknameRequest.getNickname()); //닉네임 바꿈
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
    public void withdraw(CustomUserDetail customUserDTO){
        UserEntity userEntity= userRepository.findById(customUserDTO.getUser().getUser_id())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다.")); //회원 객체 받기
        userEntity.withdraw(LocalDateTime.now()); //기존 객체를 바꾸는 거니까 빌더 필요 X
        log.info("탈퇴 처리 완료: 여부 {}, 시간 {}, ", userEntity.isDelete(), userEntity.getDeleteAt());
    }

    @Scheduled(cron= "0 0/30 * * * ?") //매시 0분, 30분마다 실행
    @Transactional
    public void deleteUser(){
        //isDelete == true && deleteAt < 지금 일자 - 3일 튜플 찾음
        List<UserEntity> users = userRepository.findAllByDeleteTrueAndDeleteAtBefore(LocalDateTime.now().minusMinutes(3));
        userRepository.deleteAll(users);
    }
    //컨트롤러에서도 repo 접근해야 돼서 만듦... 이왕 만든 김에 서비스에서도 그냥 사용
    //서비스에서는 레포로 바로 접근해도 문제 X, 컨트롤러->서비스->레포
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public Boolean existsId(String id) {
        return userRepository.existsById(id);
    }
    public Boolean existsNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
