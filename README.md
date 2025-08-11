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
## 📌 프로젝트 구조 및 API 명세서
<img width="453" height="432" alt="studyLog_ec2_rds_구조" src="https://github.com/user-attachments/assets/c28871ff-3cfb-4bc0-861b-777a76b5416d" />
<img width="1010" height="912" alt="studyLog_구조" src="https://github.com/user-attachments/assets/ab9b948e-12ed-4908-a1ca-c249fb440f4f" />


## 📌 주요 기능 흐름 소개
- **주요 기능**
    - **로그인 및 JWT**
      <img width="761" height="992" alt="studyLog_Sequence" src="https://github.com/user-attachments/assets/798209d1-2118-48f1-856b-4c7dc5a701d2" />

    - **계획 및 타이머 등록**
        <img width="761" height="956" alt="계획타이머등록_차트" src="https://github.com/user-attachments/assets/31974f6d-3a53-4858-98c5-f8223775cff7" />

  이미지 분류 모델과 바코드 API를 활용해 식재료의 카테고리를 정한다
    - **계획 자동 완료 기능 및 SSE 알림 기능**  
      <img width="761" height="904" alt="타이머스케쥴러_차트" src="https://github.com/user-attachments/assets/4ee8e4cb-da02-4af3-81d3-dfd6b27e782f" />

  두 기능을 통해 더욱 효율적인 식재료 관리 가능
    - **파일 등록**  
        <img width="1098" height="1079" alt="파일등록차트" src="https://github.com/user-attachments/assets/8ff490a9-69f8-41a1-b44c-2448591532cc" />

  표출 기준에 따라 제공받는 알림의 텍스트가 달라지며, 알림 클릭 시 선택된 냉장고 리스트로 넘어간다.
    - **공부 내용 기록**  
      <img width="310" alt="image" src="https://github.com/user-attachments/assets/44a32082-348b-4cc6-a04a-bc512d88f75d" />  
  레시피 이름순, 유통기한 임박순, 즐겨찾기 식재료순으로 정렬된 레시피를 제공한다.
## 📌 프로젝트 결과물
- **영상 링크**  
  https://drive.google.com/file/d/1gtidJY9z6wRek0JrP4__iMHpGzZFoFe9/view?usp=sharing
## 📝 프로젝트 상세 내용 및 기여 
- **노션 링크**  
  https://cooing-caraway-2f1.notion.site/1e6516e994b381f48bc7c352cbd9440c?source=copy_link
