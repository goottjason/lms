# 📘 LMS 프로젝트

> 온라인 강의 운영을 위한 학습관리시스템(LMS)을 개발합니다. 강사와 학습자는 과정 등록, 시험 제출, 과제 관리, 출결 기능을 통해 효율적인 학습 활동을 할 수 있습니다.

---

## 📌 개요

온라인 학습 환경을 위한 종합 학습관리시스템입니다. 교육생, 강사, 관리자가 각각의 역할에 따라 과정 등록, 시험, 과제, 출석 등 다양한 기능을 활용할 수 있습니다.

---

## 🛠 기술 스택

- **Back-end**: Java 17, Spring Boot, Spring Security  
- **Front-end**: Thymeleaf, JavaScript, Bootstrap  
- **Database**: MariaDB  
- **DevOps**: Gradle, GitHub Actions

---

## ✨ 기능 목록

- ✅ 사용자 로그인 및 회원가입  
- ✅ 과정 등록 및 교육생 관리  
- ✅ 시험 출제 및 응시 기능  
- ✅ 관리자 통계 대시보드  

---

## 🖼️ 스크린샷

| 로그인 화면                                                   | 관리자 홈 화면                   |
|----------------------------------------------------------|----------------------------|
| ![login](./src/main/resources//static/img/lms_login.png) | ![exam](./src/main/resources//static/img/lms_administrator_home.png) |

---

## 📁 프로젝트 구조

```bash
📦lms-project
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┣ 📂resources
 ┃ ┗ 📂test
 ┣ 📄build.gradle
 ┣ 📄README.md
```

---

## ⚙️ 설치 및 실행 방법

```bash
# 1. 클론
git clone https://github.com/alkali55/lms.git

# 2. 디렉토리 이동
cd lms
# 3. 빌드 및 실행
./gradlew build
./gradlew bootRun
```

---


## 🧩 데이터베이스 정보

- **Database**: MariaDB  
- **ERD**: [📷 ERD 보기](./src/main/resources//static/img/erd.jpg)  
- **초기 데이터**: `resources/db/init-data.sql`

---

## 🚀 배포 정보

- **환경**: aws
- **배포 주소**: 
- **구조도**: 

---

## 🧭 Git 전략 요약

- 브랜치 전략: `main`, `develop`, `feature-*`  
- 커밋 컨벤션: `feat`, `fix`, `docs`, `test`, `chore` 

- 병합 방식: 개인별 rebase squash 완료한 후 push, 이후 관리자 merge and squash
- PR 정책: 최소 1명 리뷰 후 병합, 충돌 처리 책임 명확히

---

## 👥 팀원 및 역할

| 이름 | 역할                     | GitHub |
|------|------------------------|--------|
| 류준규 | 팀장, UI 디자인, 로그인 개발     | [@joon](https://github.com/juncue) |
| 김강 | 출결 및 게시판 개발            | [@khan](https://github.com/kang855) |
| 김영재 | 게시판 및 신고기능 개발          | [@jeff](https://github.com/jeffkim98) |
| 김종원 | DB 관리자, 과정 및 교육생 관리 개발 | [@jason](https://github.com/goottjason) |
| 박유진 | 과제 및 훈련일지 개발           | [@jin](https://github.com/parkinglotlot) |
| 여성욱 | Git 관리자, 시험 및 게시판 개발   | [@corner](https://github.com/WinterI5Coming) |
| 전인수 | Git 관리자, 과정 및 알림 개발    | [@Edward](https://github.com/alkali55) |

---

## 📚 참고 자료

- [Spring 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
- [Swagger 문서](https://swagger.io/specification/)  
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)

---

## 📎 기타 문서

- [ERD](https://www.notion.so/ERD-1fee9988a01e80ae8befe88667bbc013?source=copy_link)  
- [Figma UI 설계](https://www.figma.com/board/qKdHVQm8NrHptUlx5HJvga/Goott5-Final-Project---LMS%EC%8B%9C%EC%8A%A4%ED%85%9C?node-id=0-1&t=VQtjZSXRvgQzyTSy-1)

---