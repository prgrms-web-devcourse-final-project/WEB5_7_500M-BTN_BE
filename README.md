# 🚀 5억년 버튼 (500 Million Year Button) - 백엔드

<div align="center">

  <!-- 프로젝트 로고 또는 메인 이미지 -->
  <img src="https://via.placeholder.com/400x200/4285F4/FFFFFF?text=5억년+버튼" alt="5억년 버튼 로고" width="400"/>

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.1-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-blue)
![Redis](https://img.shields.io/badge/Redis-red)
![JPA](https://img.shields.io/badge/JPA-yellowgreen)
![WebSocket](https://img.shields.io/badge/WebSocket-blueviolet)

  <p><strong>데브코스 7회차 최종 프로젝트 - '5억년 버튼'의 백엔드 레포지토리입니다.</strong></p>

[🌐 라이브 데모](https://your-demo-url.com) • [📚 API 문서](https://your-api-docs.com) • [🎨 프론트엔드](https://github.com/your-frontend-repo)
</div>

---

## 📝 프로젝트 소개

'5억년 버튼'은 실시간 상호작용 기능을 갖춘 웹 애플리케이션으로, 사용자들이 함께 참여하고 소통할 수 있는 플랫폼입니다. 이 프로젝트는 데브코스 7회차 최종 프로젝트로
개발되었습니다.

### ✨ 주요 기능

- 🔐 **소셜 로그인** - OAuth2를 통한 간편한 로그인
- 💬 **실시간 채팅** - WebSocket을 활용한 실시간 소통
- 📱 **반응형 디자인** - 모든 디바이스에서 최적화된 경험
- 🔔 **알림 시스템** - 실시간 알림 및 푸시 메시지
- 📊 **대시보드** - 사용자 활동 및 통계 대시보드

---

## 🖼️ 스크린샷 & 데모

<div align="center">

### 📱 메인 화면

<img src="https://via.placeholder.com/800x400/FF6B6B/FFFFFF?text=메인+화면+스크린샷" alt="메인 화면" width="800"/>

### 💬 실시간 채팅

<img src="https://via.placeholder.com/800x400/4ECDC4/FFFFFF?text=실시간+채팅+화면" alt="채팅 화면" width="800"/>

### 📊 대시보드

<img src="https://via.placeholder.com/800x400/45B7D1/FFFFFF?text=대시보드+화면" alt="대시보드" width="800"/>

</div>

<!-- 데모 GIF 추가 예시 -->

### 🎬 데모 영상

<div align="center">
  <img src="https://via.placeholder.com/600x300/96CEB4/FFFFFF?text=데모+GIF" alt="프로젝트 데모" width="600"/>
  <p><em>5억년 버튼의 주요 기능 데모</em></p>
</div>

---

## 🛠️ 기술 스택

<div align="center">

| 분야                | 기술 스택                                                                                                                                                                                                                                 |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Backend**       | ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)         |
| **Database**      | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)                                 |
| **Security**      | ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=oauth&logoColor=white) |
| **Communication** | ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white)                                                                                                                        |
| **Tools**         | ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)                           |

</div>

### 📋 상세 기술 스택

<details>
<summary><strong>🖥️ 백엔드</strong></summary>

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.3.1
- **DB**: MySQL, Redis
- **ORM**: Spring Data JPA
- **인증/인가**: Spring Security, OAuth2
- **실시간 통신**: WebSocket
- **API 문서화**: Springdoc OpenAPI (Swagger)
- **기타**: Spring Retry, Lombok

</details>

<details>
<summary><strong>🔧 개발 도구</strong></summary>

- **빌드 도구**: Gradle
- **컨테이너화**: Docker, Docker Compose
- **테스트**: JUnit 5, Spring Boot Test
- **CI/CD**: GitHub Actions (예정)
- **모니터링**: Actuator, Micrometer

</details>

---

## 🏗️ 시스템 아키텍처

<div align="center">
  <img src="https://via.placeholder.com/800x500/FFA07A/FFFFFF?text=시스템+아키텍처+다이어그램" alt="시스템 아키텍처" width="800"/>
</div>

### 📁 프로젝트 구조

```
📦 matjalalzz/
├── 📂 src/main/java/shop/matjalalzz/
│   ├── 🌐 global/             # 공통 설정, 유틸리티
│   │   ├── config/           # 설정 클래스
│   │   ├── exception/        # 예외 처리
│   │   ├── security/         # 보안 설정
│   │   └── utils/            # 유틸리티 클래스
│   ├── 👤 user/              # 사용자 관리
│   │   ├── api/              # 컨트롤러
│   │   ├── app/              # 서비스
│   │   ├── dao/              # 레포지토리
│   │   └── domain/           # 엔티티, DTO
│   ├── 📅 reservation/       # 예약 시스템
│   ├── 💬 comment/           # 댓글 기능
│   ├── ⭐ review/            # 리뷰 시스템
│   └── 🏪 shop/             # 상점 관리
├── 📂 src/main/resources/
│   ├── application.yml       # 애플리케이션 설정
│   ├── logback.xml          # 로깅 설정
│   └── websocket-Test.html  # WebSocket 테스트
└── 📂 src/test/             # 테스트 코드
```

---

## 🚀 시작하기

### 📋 필수 조건

- ☕ **Java 21** 이상
- 🐳 **Docker & Docker Compose** (권장)
- 🗄️ **MySQL 8.0** 이상
- 📦 **Redis 6.0** 이상

### ⚡ 빠른 시작

1. **레포지토리 클론**
   ```bash
   git clone https://github.com/your-username/WEB5_7_500M-BTN_BE.git
   cd WEB5_7_500M-BTN_BE/matjalalzz
   ```

2. **환경 변수 설정**
   ```bash
   cp .env.example .env
   # .env 파일을 열어서 필요한 값들을 설정하세요
   ```

3. **Docker로 실행 (권장)**
   ```bash
   docker-compose up -d
   ```

4. **수동 실행**
   ```bash
   # 데이터베이스 및 Redis 실행 후
   ./gradlew clean build
   ./gradlew bootRun
   ```

5. **애플리케이션 확인**
    - 🌐 서버: http://localhost:8080
    - 📚 API 문서: http://localhost:8080/swagger-ui.html
    - 💬 WebSocket 테스트: http://localhost:8080/websocket-Test.html

---

## 📊 API 문서

<div align="center">
  <img src="https://via.placeholder.com/600x300/20B2AA/FFFFFF?text=Swagger+API+문서" alt="API 문서" width="600"/>
</div>

서버 실행 후 다음 URL에서 상세한 API 문서를 확인할 수 있습니다:

**🔗 [Swagger UI](http://localhost:8080/swagger-ui.html)**

### 주요 API 엔드포인트

- `GET /api/v1/users` - 사용자 목록 조회
- `POST /api/v1/auth/login` - 로그인
- `GET /api/v1/reservations` - 예약 목록 조회
- `POST /api/v1/comments` - 댓글 작성
- `WebSocket /ws/chat` - 실시간 채팅

---

## 🧪 테스트

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests CommentServiceTest

# 테스트 리포트 확인
./gradlew test jacocoTestReport
```

### 테스트 커버리지

- **단위 테스트**: Service, Repository 계층
- **통합 테스트**: API 엔드포인트
- **보안 테스트**: 인증/인가 로직

---

## 📈 성능 및 모니터링

<div align="center">
  <img src="https://via.placeholder.com/600x300/FFB6C1/000000?text=성능+모니터링+대시보드" alt="모니터링" width="600"/>
</div>

- **Actuator**: 애플리케이션 상태 모니터링
- **Micrometer**: 메트릭 수집
- **로깅**: Logback을 통한 구조화된 로깅

---

## 🤝 기여하기

프로젝트에 기여하고 싶으시다면 다음 절차를 따라주세요:

1. 🍴 Fork the Project
2. 🌿 Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. ✅ Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push to the Branch (`git push origin feature/AmazingFeature`)
5. 🔄 Open a Pull Request

---

## 👥 팀원

<div align="center">

| 역할        | 이름   | GitHub                                       |
|-----------|------|----------------------------------------------|
| **팀장**    | 개발자1 | [@github-id1](https://github.com/github-id1) |
| **백엔드**   | 개발자2 | [@github-id2](https://github.com/github-id2) |
| **백엔드**   | 개발자3 | [@github-id3](https://github.com/github-id3) |
| **프론트엔드** | 개발자4 | [@github-id4](https://github.com/github-id4) |

</div>

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 📞 문의하기

프로젝트에 대한 질문이나 제안사항이 있으시면 언제든지 연락주세요!

- 📧 **이메일**: team@5billionyearbutton.com
- 💬 **디스코드**: [팀 디스코드 서버](https://discord.gg/your-server)
- 📝 **이슈**: [GitHub Issues](https://github.com/your-username/WEB5_7_500M-BTN_BE/issues)

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! ⭐**

![GitHub stars](https://img.shields.io/github/stars/your-username/WEB5_7_500M-BTN_BE?style=social)
![GitHub forks](https://img.shields.io/github/forks/your-username/WEB5_7_500M-BTN_BE?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/your-username/WEB5_7_500M-BTN_BE?style=social)

</div>
