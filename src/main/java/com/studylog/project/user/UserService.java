package com.studylog.project.user;

import com.studylog.project.global.exception.DuplicateException;
import com.studylog.project.global.exception.InvalidRequestException;
import com.studylog.project.global.exception.LoginFaildException;
import com.studylog.project.global.exception.MailException;
import com.studylog.project.jwt.CustomUserDetail;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate; //제네릭 타입 명시

    /* @RequiredArgsConstructor 사용으로 생략
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }*/

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
        {
            throw new MailException("인증 세션이 만료됐거나 인증된 메일이 아닙니다.");
        }
        String encryptedPw= passwordEncoder.encode(user.getPw());
        UserEntity userEntity = user.toEntity();
        userEntity.setEncodedPw(encryptedPw); //빌더 객체 pw 값 바뀜
        userRepository.save(userEntity);
    }

    //로그인한 유저 닉네임 반환
    public String getNickname(LogInRequest request){
        UserEntity userEntity= userRepository.findById(request.getId())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("[%s]에 해당하는 회원을 찾을 수 없습니다.", request.getId())));
        return userEntity.getNickname();
    }

    //비밀번호 확인
    public void validatePw(UserEntity user, String pw) {
        if (passwordEncoder.matches(pw, user.getPw())) {
            log.info(String.format("id: [%s], 로그인 성공", user.getId()));
        }
        else{
            log.warn(String.format("id: [%s], 로그인 실패", user.getId()));
            throw new LoginFaildException("비밀번호가 일치하지 않습니다.");
        }
    }

    //비밀번호 변경
    public void changePw(CustomUserDetail customUserDTO, UpdatePwRequest pwRequest) { //암호화된 비번(DTO), 평문 비번(request)
        if (passwordEncoder.matches(pwRequest.getCurrentPw(), customUserDTO.getPassword())){
            //평문 비번과 암호화 비번이 동일하다면
            if(pwRequest.getCurrentPw().equals(pwRequest.getNewPw())){
                throw new InvalidRequestException("기존 비밀번호와 새로운 비밀번호가 일치합니다.");
            }
            String encryptedPw= passwordEncoder.encode(pwRequest.getNewPw()); //새 비번 암호화
            UserEntity userEntity= customUserDTO.getUser(); //principal의 user 객체를 entity에 넣음
            userEntity.changePw(encryptedPw);
            log.info("changePw: 비밀번호 변경 완료");
        }
        else{ //현재 비번 일치 X
            log.info("changePw: 비밀번호 변경 실패");
            throw new InvalidRequestException("기존 비밀번호와 일치하지 않습니다.");
        }
    }

    //닉네임 변경
    public void changeNickname(CustomUserDetail customUserDTO,UpdateNicknameRequest nicknameRequest) {
        String currentNick= customUserDTO.getUser().getNickname();
        if(currentNick.equals(nicknameRequest.getNickname())){
            //기존 닉네임 동일 시
            throw new InvalidRequestException("현재 닉네임과 동일합니다.");
        }
        //닉네임 중복 시
    }

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
