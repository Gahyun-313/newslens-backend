---
name: 🐛 Bug Report
about: 버그를 발견했을 때 사용합니다
title: "fix(<scope>): <버그 요약>"
labels: bug
assignees: ""
---

## 🐞 버그 설명
> 어떤 문제가 발생했는지 간략히 작성해주세요.

- 문제 요약:
- 발생 위치:
- 영향 범위:

---

## 🔁 재현 방법
> 버그를 재현하는 단계를 작성해주세요.

1.
2.
3.

---

## ✅ 기대 동작
> 원래 어떻게 동작해야 하는지 작성해주세요.

---

## ❌ 실제 동작
> 실제로 어떻게 동작하는지 작성해주세요.

---

## 📸 스크린샷 / 📜 로그
> 해당하는 경우 첨부해주세요.

**에러 메시지**:
```
에러 로그 붙여넣기
```

**스택 트레이스**:
```
스택 트레이스 붙여넣기
```

**API 응답**:
```json
{
  "error": "..."
}
```

---

## 🌍 실행 환경
- OS: (Windows / macOS / Linux)
- Java 버전: (예: Java 17)
- Spring Boot 버전: (예: 3.5.11)
- DB: (H2 / MySQL)
- 빌드 도구: (Gradle / Maven)

---

## 🎯 관련 Scope
> 해당하는 책임 영역을 체크해주세요.

**피드 관련**
- [ ] `domestic-feed` (국내 뉴스)
- [ ] `global-feed` (해외 뉴스)
- [ ] `cluster` (클러스터링)

**계층 관련**
- [ ] `domain` (Entity)
- [ ] `repository` (Repository)
- [ ] `service` (Service)
- [ ] `controller` (Controller)
- [ ] `dto` (DTO)

**인프라 관련**
- [ ] `config` (설정)
- [ ] `setup` (초기 설정)
- [ ] `build` (빌드)
- [ ] `deploy` (배포)

---

## ⚠ 영향도
- [ ] Low (일부 기능 영향)
- [ ] Medium (주요 기능 영향)
- [ ] High (서버 크래시 / 서비스 불가)

---

## 💡 추가 참고 사항 (선택)
> 원인으로 추정되는 부분이나 참고 링크가 있다면 작성해주세요.

