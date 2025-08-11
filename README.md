## 📌 프로젝트 소개
1. **프로젝트명**: 스터디 로그
2. **프로젝트 인원**: 1명
3. **프로젝트 기간**: 2025.06~
4. **프로젝트 목적**: 흩어진 공부 환경을 하나로 통합해, 사용자의 학습 피로도를 줄이고 계획-실천-기록-성취 사이클을 통해 루틴을 만들어주는 학습 관리 도구입니다. 
5. **사용 기술**
    - 백엔드: Java, Spring Boot, Spring Security, JWT, JPA, QueryDSL, SSE (타이머 완료 이벤트 전송)
    - 프론트: React (개발 중)
    - DB: MariaDB(AWS RDS), Redis
    - Infra/DevOps: EC2, RDS, Nginx, Github Actions, Certbot
## 📌 API 명세서 및 프로젝트 구조
- **Swagger API 문서 (테스트 가능)**</br>
https://ppyong11.github.io/studyLog_swagger_pages/ </br></br>
- **프로젝트 구조**</br>
    <img width="761" height="912" alt="studyLog_구조" src="https://github.com/user-attachments/assets/ab9b948e-12ed-4908-a1ca-c249fb440f4f" /> </br>
    서버 배포와 HTTPS 적용을 위해 EC2와 도메인을 사용했으며, 코드가 바뀔 때마다 배포하는 번거로움을 줄이기 위해 GitHub Actions 활용한 CI/CD를 도입했습니다.</br>
    Nginx의 리버스 프록시를 이용해 엔드포인트에 따라 프론트와 백엔드로 분기해 클라이언트의 요청에 응답합니다. 
    </br></br>
    <img width="415" height="432" alt="studyLog_ec2_rds_구조" src="https://github.com/user-attachments/assets/c28871ff-3cfb-4bc0-861b-777a76b5416d" /> </br>
    동일 VPC 내에 외부와 통신하는 Public Subnet에 웹 서버를 두고, 외부에서 접근이 안 되는 Private Subnet에 DB 서버를 둬 네트워크를 분리해 보안을 강화했습니다. 


## 📌 주요 기능 흐름 소개
- **주요 기능**
    - **로그인 및 JWT**
      <img width="1000" height="992" alt="studyLog_Sequence" src="https://github.com/user-attachments/assets/798209d1-2118-48f1-856b-4c7dc5a701d2" /></br>
      로

    - **계획 및 타이머 등록**
        <img width="1000" height="956" alt="계획타이머등록_차트" src="https://github.com/user-attachments/assets/31974f6d-3a53-4858-98c5-f8223775cff7" /></br>
        dd

    - **계획 자동 완료 기능 및 SSE 알림 기능**  
      <img width="1000" height="904" alt="타이머스케쥴러_차트" src="https://github.com/user-attachments/assets/4ee8e4cb-da02-4af3-81d3-dfd6b27e782f" /></br>
      dd
    - **파일 등록**  
        <img width="1098" height="1079" alt="파일등록차트" src="https://github.com/user-attachments/assets/8ff490a9-69f8-41a1-b44c-2448591532cc" /></br>
        dd

    - **공부 내용 기록**</br>
        <img width="310" alt="image" src="https://github.com/user-attachments/assets/44a32082-348b-4cc6-a04a-bc512d88f75d" /></br>
## 📝 프로젝트 상세 내용 및 기여 
- **노션 링크**  
  https://cooing-caraway-2f1.notion.site/23b516e994b380efb57ce2d2123866c2?source=copy_link
