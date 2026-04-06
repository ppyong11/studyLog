## 📌 프로젝트 소개
1. **프로젝트 주소**: https://studylog.hyeoncode.dev/
2. **프로젝트명**: 스터디 로그
3. **프로젝트 인원**: 1명
4. **프로젝트 기간 (백엔드)**: 2025.06~2026.03
5. **프로젝트 목표**: 흩어진 공부 환경을 통합하여 계획-실천-기록-성취 사이클을 만들어 학습 효율을 높이는 학습 관리 서비스입니다.
6. **사용 기술**
    - Backend: Java, Spring Boot, Spring Security, JPA, QueryDSL
    - Frontend: React, Next.js
    - Communication: RESTful API, SSE (알림 이벤트 발송), WebSocket (브라우저 종료 감지)
    - Database: MariaDB, Redis
    - Infra / DevOps: AWS EC2, RDS, Nginx, GitHub Actions, Certbot
    - Monitoring: Prometheus, Grafana
    - Documentation / Design: Swagger, GitHub Pages, Draw.io, ERD Editor, Canva
6. **문서화, 설계 도구**
   - Swagger, Github Pages, Draw.io, Canva, VS code (ERD Editor)<br>
## 📌 API 명세서 및 ERD
- **Swagger API 문서**</br>
    https://studylog-swagger.hyeoncode.dev/</br></br>
- **DB ERD**
    <img width="750" height="370" alt="user_plan_timer ERD" src="https://github.com/user-attachments/assets/71b09598-9ec0-48e4-b348-3b2ce708d8b5" /> </br>
    user 테이블: 모든 테이블과 1:N 관계로 서비스 전체의 데이터 소유권을 가집니다.</br>
    plan / timer 테이블: 선택적 1:1 관계(nullable FK)를 통해 계획이 설정되지 않은 타이머와 타이머가 없는 계획을 모두 수용해 서비스 유연성을 확보했습니다.</br>
    <img width="650" height="231" alt="board_file ERD" src="https://github.com/user-attachments/assets/52b1e0e2-bc9e-4075-8198-a3d160d0843c" /></br>
    board / file 테이블: 1:N 관계로 다중 파일 업로드가 가능하며 게시글 삭제 시 관련 파일 데이터도 함께 삭제되도록 CASCADE 옵션을 적용해 데이터 생명주기를 일치시켰습니다.</br>
    <img width="600" height="231" alt="category_notification ERD" src="https://github.com/user-attachments/assets/2fcb006a-0d7e-4c27-9359-60b985ac0021" /> </br>
    category 테이블: 계획, 타이머, 게시글 테이블과 1:N 관계로 데이터를 분류할 수 있으며 회원가입 시 시스템 기본 카테고리(기타)가 자동 생성되도록 하여 사용자가 즉시 데이터를 등록할 수 있도록 초기 환경을 제공합니다. </br>
    Notification 테이블: 계획 완료 이벤트 발생 시 생성되는 알림 데이터를 저장하며 알림 이력을 관리할 수 있도록 설계했습니다.

## 📌 시스템 구조
<img width="661" height="712" alt="studyLog_구조" src="https://github.com/user-attachments/assets/231700a5-bc90-4c5b-994f-43e6a1781a0e" /> </br>
서버 배포와 HTTPS 적용을 위해 EC2와 도메인을 사용했으며, 코드가 바뀔 때마다 배포하는 번거로움을 줄이기 위해 GitHub Actions 활용한 CI/CD를 도입했습니다.</br>
Nginx의 리버스 프록시를 이용해 엔드포인트에 따라 FrontEnd Server와 API Server로 분기해 클라이언트의 요청에 응답합니다. </br></br>

## 📌 주요 기능 흐름 소개
### **JWT 기반 인증 및 토큰 재발급 시스템**<br>
  <img width="1000" height="992" alt="studyLog_Sequence" src="https://github.com/user-attachments/assets/cdf1d180-d456-4a8d-9f6c-740823ffe66c" /></br>
- **보안 중심 인증 구조**
  - JWT 기반 인증 구조를 설계하고 Refresh Token을 Redis에 저장해 세션 상태를 관리했습니다.
  - Refresh Token Rotation(RTR)을 적용해 토큰 탈취 시 재사용 공격을 방지했습니다.
  - Access Token 블랙리스트를 운영해 로그아웃 이후 토큰 사용을 차단했습니다.
- **인증 흐름 안정성 개선**
    - Axios Interceptor를 활용해 401 응답을 중앙에서 처리하고 토큰 재발급 로직을 일관되게 관리했습니다.
    - 다중 API 호출 환경에서 발생하던 중복 로그아웃 및 무한 리다이렉트 문제를 상태 플래그 기반으로 방어했습니다.
    - Zustand 전역 상태를 활용해 로그인 상태 변경 시 UI가 자동 동기화되도록 구현했습니다.
  
### **타이머 동기화 및 실시간 알림 시스템(WebSocket & SSE)**
<img width="886" height="670" alt="타이머-알림 다이어그램" src="https://github.com/user-attachments/assets/d07bf818-a973-4d78-9e0a-b239bb7025f3" /><br>
- WebSocket 연결 종료 이벤트를 활용해 브라우저 종료 시 실행 중인 타이머를 즉시 종료하도록 구현했습니다.
- 네트워크 장애 등 예외 상황을 대비해 5분 주기의 스케줄러로 계획 완료 여부를 재검증하는 이중 검증 구조를 설계해 데이터 정합성을 확보했습니다.
- 계획 완료 시 SSE로 알림 이벤트를 전송해 사용자에게 실시간으로 상태 변화를 전달하도록 구성했습니다.
- 실행 중인 타이머가 있을 때만 WebSocket 연결을 유지하도록 해 불필요한 연결과 서버 자원 사용을 줄였습니다.

### **게시글 파일 업로드 및 스토리지 관리 시스템 (게시글 & 파일 업로드)**
<img width="728" height="447" alt="파일 관리 다이어그램" src="https://github.com/user-attachments/assets/d286e41f-9c07-4cde-a5a3-5a400293269c" /></br>
- Draft ID 기반 임시 저장 구조를 적용해 게시글 등록 시 파일과 게시글을 안전하게 매핑하도록 설계했습니다.
- 게시글과 연결되지 않은 임시 파일을 주기적으로 삭제하는 스케줄러를 구현해 스토리지 누적 문제를 방지했습니다.
- 파일 업로드 시 확장자 필터링과 용량 제한을 적용해 서버 보안을 강화했습니다.
### **이메일 인증 기반 회원가입 및 Soft Delete 탈퇴 시스템**
<img width="951" height="511" alt="회원가입 다이어그램" src="https://github.com/user-attachments/assets/5c4b5931-dca1-4ebb-ba38-cca09c23ade4" /><br>
- 이메일 인증 코드 및 상태를 Redis에 TTL로 관리해 인증 코드와 상태를 안전하게 제어했습니다.
- 회원 탈퇴 시 Soft Delete를 적용하고 일정 기간 이후 스케줄러로 영구 삭제하도록 설계했습니다.
- 유예 기간 내 재로그인 시 계정이 복구되도록 구현했습니다.
### **조건 기반 검색 및 카테고리 관리 기능**
- QueryDSL을 활용해 이름, 날짜, 카테고리 등 다양한 조건을 조합할 수 있는 동적 검색 기능을 구현했습니다.
- 사용자 정의 카테고리 관리 기능을 제공해 개인화된 데이터 분류가 가능하도록 설계했습니다.



## 📝 프로젝트 상세 내용 
- **노션 링크** </br>
  https://www.notion.so/32a516e994b3816e857ff7197504b804?source=copy_link
- **구동 gif**
    - 계획-타이머 연동과 자동 알림
    ![플랜-타이머영상](https://github.com/user-attachments/assets/0f07af4b-9b76-4e4d-bac5-446df49a788e)

    - 게시글-파일 등록 및 저장
  ![게시글-파일영상](https://github.com/user-attachments/assets/f7240dc7-fec8-4f65-ae17-c3b678c669ff)
