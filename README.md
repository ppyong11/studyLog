## 📌 프로젝트 소개
1. **프로젝트명**: 스터디 로그
2. **프로젝트 인원**: 1명
3. **프로젝트 기간 (백엔드)**: 2025.06~2025.08 (10주)
4. **프로젝트 목적**: 흩어진 공부 환경을 통합하여 계획-실천-기록-성취 사이클을 만들어 학습 효율을 높이는 학습 관리 서비스입니다.
5. **사용 기술**
    - 백엔드: Java 17, Spring Boot 3.2.5, Spring Security, JWT, JPA, QueryDSL, SSE (타이머 완료 이벤트 전송)
    - 프론트: React (개발 예정)
    - DB: MariaDB, Redis
    - Infra/DevOps: EC2, RDS, Nginx, Github Actions, Certbot
    - 모니터링: Prometheus, Grafana
6. **문서화, 설계 도구**
   - Swagger, Github Pages, Draw.io, Canva, VS code (ERD Editor)<br>
    
현재 백엔드 API만 개발해 자체 도메인에서 테스트 운영 중이며, 추후 보안 강화와 프론트엔드를 구현해 서비스 고도화를 계획하고 있습니다.
## 📌 API 명세서 및 ERD
- **Swagger API 문서 **</br>
    https://studylog-swagger.hyeoncode.dev/</br></br>
- **DB ERD**
    <img width="1600" height="907" alt="스터디로그 ERD" src="https://github.com/user-attachments/assets/76da10aa-5c12-4046-8ac3-a3ba1dd92a25" />

    User 테이블: 회원 정보 테이블. Lap 테이블을 제외한 모든 테이블과 1:N 관계 <br>

    Category 테이블: 카테고리 저장 테이블. 회원가입 시 <기타> 카테고리가 자동 생성되며 삭제가 불가합니다. Board, Plan, Timer 테이블과 1:N 관계<br>

    Board 테이블: 게시글 저장 테이블. 파일 등록 기능을 제공해 File 테이블과 1:N 관계<br>

    File 테이블: 게시글에 업로드한 파일 저장 테이블. <br>

    Plan 테이블: 계획 저장 테이블. Timer 테이블과 0:1 관계 (양방향 모두 선택적 관계로 서로가 없어도 존재할 수 있음)<br>

    Timer 테이블: 타이머 사용 기록 저장 테이블. 계획 설정 시 계획의 카테고리와 타이머에 등록하려는 카테고리가 일치해야 함. Lap,     Notification 테이블과 1:N 관계<br>

    Lap 테이블: 타이머 랩 기록 저장 테이블 <br>

    Notification 테이블: 계획 완료 처리로 수신한 알림을 담는 테이블. 알림을 수신한 조건에 따라 timer 값이 null이 될 수 있음. <br>
## 📌 시스템 구조
<img width="761" height="912" alt="studyLog_구조" src="https://github.com/user-attachments/assets/f5b6b9ab-a9ac-4dfb-9071-3a7d19e60796" /> </br>
서버 배포와 HTTPS 적용을 위해 EC2와 도메인을 사용했으며, 코드가 바뀔 때마다 배포하는 번거로움을 줄이기 위해 GitHub Actions 활용한 CI/CD를 도입했습니다.</br>
Nginx의 리버스 프록시를 이용해 엔드포인트에 따라 프론트와 백엔드로 분기해 클라이언트의 요청에 응답합니다. </br></br>
<img width="415" height="432" alt="studyLog_ec2_rds_구조" src="https://github.com/user-attachments/assets/c28871ff-3cfb-4bc0-861b-777a76b5416d" /> </br>
    동일 VPC 내에 외부와 통신하는 Public Subnet에 웹 서버를 두고, 외부에서 접근이 안 되는 Private Subnet에 DB 서버를 둬 네트워크를 분리해 보안을 강화했습니다.<br><br>


## 📌 주요 기능 흐름 소개
- **메인 화면 진입 시 토큰 유무에 따른 처리, 로그인 시도, 토큰 재발급 Sequence Diagram**
      <img width="1000" height="992" alt="studyLog_Sequence" src="https://github.com/user-attachments/assets/798209d1-2118-48f1-856b-4c7dc5a701d2" /></br>
      - 메인화면 진입 시<br>
        a. 로그인 상태일 땐 사용자의 하루 계획과 주간 리포트 반환  
        b. 로그아웃 상태일 땐 정적 메인페이지 반환 (홍보 페이지)<br><br>
      - 로그인 로직<br>
        a. id, pw에 해당하는 유저가 있을 시, jwt 생성 & redis에 refresh token 저장 & 쿠키에 담아 반환
        b. 없는 유저일 시 400 에러 코드 반환<br><br>
      - 토큰 재발급<br>
        사용자가 로그인 연장을 직접 요청할 수 있어, 토큰 재발급 시 access token을 받도록 설정했습니다.
        a. access token, refresh 토큰 검증 (로그아웃한 유저거나 만료된 토큰, 재발급에 이미 사용된 토큰인지 확인하는 과정)<br>
        b. 재발급에 사용된 access token은 블랙리스트에 저장되고 redis에 저장한 refresh token 삭제 후 새 refresh token 저장<br>
        c. 새로 발급된 토큰들을 쿠키에 담아 반환<br><br>
- **계획 및 타이머 등록**
  <img width="1000" height="956" alt="계획타이머등록_차트" src="https://github.com/user-attachments/assets/cf98003e-15a6-4638-a63f-e2e0711c6b20" /></br>
  계획을 등록하면 타이머에 설정해 계획별 시간 관리가 수월해지고, 일간/주간/월간 리포트와 날짜별 달성률을 확인할 수 있습니다. <br>
  타이머 실행 중 여러 구간별(랩) 기록이 가능하며, 랩이 삭제돼도 타이머의 총 경과 시간엔 영향이 없습니다. 5분마다 스케쥴러를 통한 동기화 및 수동 동기화와 리셋 처리가 가능하고 동기화 시 경과 시간 갱신과 계획 완료 여부 처리를 진행합니다. 

- **타이머 작동 및 계획 자동 완료 처리와 SSE 알림 관련 기능**
    <img width="1000" height="956" alt="계획타이머등록_차트" src="https://github.com/user-attachments/assets/e5944a21-d7b7-4cf7-9618-b419247ad1e3" /></br>
  종료된 타이머는 재실행이 불가하며, 실행 중인 상태에만 정지 및 종료가 가능합니다. 계획이 설정된 테이블은 정지 및 종료, 동기화 시 계획의 목표 시간과 타이머 경과 시간을 비교해 자동 계획 달성 처리와 알림이 발송됩니다. 발송된 알림은 DB에 저장되어 누적 알림 확인 및 관리가 가능합니다.<br><br>
  - **누적 알림 기능** <br>
    1. 동기화를 통해 계획이 완료 처리된 경우<br>
        사용자가 타이머를 정지/종료할 수 있도록 알림 클릭 시 해당 타이머로 이동됩니다. 만약 타이머를 삭제할 시, 알림을 클릭해도 타이머에 진입이 불가합니다.
    2. 타이머를 직접 정지/종료해 계획이 완료 처리한 경우<br>
       알림 클릭 시 사용자의 계획페이지로 이동됩니다. 타이머에 설정한 계획 페이지가 아닌, 전체 계획 페이지로 이동되기에 계획을 삭제해도 영향이 없습니다.
    3. 알림 전체 삭제 및 일부 삭제 가능
    4. 미확인 알림 개수 표출

- **공부 내용 기록 게시글 / 파일 등록**  
    <img width="1098" height="1079" alt="파일등록차트" src="https://github.com/user-attachments/assets/8ff490a9-69f8-41a1-b44c-2448591532cc" /></br>
  게시글 등록 학습 내용을 자료와 함께 남겨 복습을 용이하게 합니다.<br>
  - **파일 등록 로직** <br>
    1. 게시글 작성 폼 진입 시 UUID(draft_id)를 생성한 후, 파일 업로드 시 해당 UUID를 요청값으로 사용해 파일 테이블의 draft_id 필드에 넣고, isdraft=1 (등록되지 않은 파일) 상태로 저장한다.
    2. 게시글 등록 API 호출 시 서버가 게시글의 UUID(draft_id)를 요청값으로 사용해 파일 테이블의 draft_id 값과 비교한 후,알맞으면 draft_id를 삭제하고 board_id를 채우며 isdraft를 0으로 변경(등록된 파일)한다.
    3. 게시글 수정 시 파일 업로드는 board_id를 요청값으로 사용해 게시글과 파일이 매칭될 수 있지만, isdraft가 0이 되려면 수정 API 호출 후 서버에서 처리해야 한다.<br>
    ➡️ is_draft= 0: 게시글 조회 시, 업로드된 파일이 표출되지 않음.
      
## 📝 프로젝트 상세 내용 
- **노션 링크**  
  https://cooing-caraway-2f1.notion.site/23b516e994b380efb57ce2d2123866c2?source=copy_link
