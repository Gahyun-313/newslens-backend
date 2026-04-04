# Spring Boot 예외 처리 아키텍처

> NewsLens 백엔드의 중앙 집중식 예외 처리 시스템

---

## 📑 목차

1. [개요](#개요)
2. [아키텍처](#아키텍처)
3. [구성 요소](#구성-요소)
4. [동작 흐름](#동작-흐름)
5. [예외별 처리 전략](#예외별-처리-전략)
6. [왜 필요한가](#왜-필요한가)
7. [구현 체크리스트](#구현-체크리스트)

---

## 개요

### 목적
- 일관된 에러 응답 형식 제공
- 중앙 집중식 예외 관리
- 클라이언트 친화적인 에러 메시지

### 핵심 원칙
```
✓ 모든 에러는 동일한 JSON 형식으로 반환
✓ 예외 처리 로직은 한 곳(GlobalExceptionHandler)에 집중
✓ 명확한 HTTP 상태 코드 사용
✓ 비즈니스 로직과 예외 처리 분리
```

---

## 아키텍처

### 전체 구조

```
┌─────────────────────────────────────────────────────────┐
│                        Client                            │
└─────────────────────────────────────────────────────────┘
                          ↓ HTTP Request
┌─────────────────────────────────────────────────────────┐
│                    DispatcherServlet                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Controller                          │
│                    (비즈니스 로직)                        │
└─────────────────────────────────────────────────────────┘
                          ↓
                    ❌ 예외 발생!
                          ↓
┌─────────────────────────────────────────────────────────┐
│             GlobalExceptionHandler                       │
│              (@RestControllerAdvice)                     │
│                                                           │
│  • 예외 감지 (@ExceptionHandler)                         │
│  • ErrorResponse 생성                                    │
│  • HTTP 상태 코드 매핑                                    │
│  • 로깅                                                   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    ErrorResponse                         │
│                   (표준화된 JSON)                         │
└─────────────────────────────────────────────────────────┘
                          ↓ HTTP Response
┌─────────────────────────────────────────────────────────┐
│                        Client                            │
└─────────────────────────────────────────────────────────┘
```

### 패키지 구조

```
com.newslens_backend
└── exception/
    ├── ErrorResponse.java              # 에러 응답 DTO
    ├── NewsNotFoundException.java      # 커스텀 예외
    └── GlobalExceptionHandler.java     # 중앙 예외 처리
```

---

## 구성 요소

### 1. ErrorResponse (에러 응답 DTO)

**역할**: 모든 에러를 동일한 형식으로 반환

**필드 구성**:
- `timestamp`: 에러 발생 시간
- `status`: HTTP 상태 코드 (404, 400, 500 등)
- `error`: 에러 종류 ("Not Found", "Bad Request")
- `message`: 상세 메시지 (클라이언트가 이해 가능한 문구)
- `path`: 요청 경로

**응답 예시**:
```json
{
  "timestamp": "2024-03-17T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cluster not found: invalid-id",
  "path": "/api/news/cluster/invalid-id"
}
```

**설계 포인트**:
- static 팩토리 메서드 (`of()`)로 생성 편의성 제공
- timestamp 자동 생성
- 불변 객체 (final 필드)

---

### 2. NewsNotFoundException (커스텀 예외)

**역할**: 뉴스 관련 리소스를 찾을 수 없을 때 발생

**상속 관계**:
```
RuntimeException (Unchecked Exception)
    ↑
NewsNotFoundException
```

**왜 RuntimeException을 상속하나?**
- try-catch 강제하지 않음 → 코드 간결성
- Spring이 자동으로 처리
- 비즈니스 로직에 집중 가능

**vs 다른 예외들**:
- `IllegalArgumentException`: 일반적인 잘못된 인자 → 의미 모호
- `NewsNotFoundException`: 뉴스를 못 찾음 → 의미 명확

**사용 시나리오**:
- 존재하지 않는 클러스터 조회
- 삭제된 뉴스 접근
- 유효하지 않은 뉴스 ID

---

### 3. GlobalExceptionHandler (중앙 예외 처리)

**역할**: 모든 Controller의 예외를 한 곳에서 처리

**어노테이션**:
- `@RestControllerAdvice`: 모든 @RestController의 예외 처리
- `@ExceptionHandler(예외.class)`: 특정 예외를 처리하는 메서드

**처리하는 예외**:
1. `NewsNotFoundException` → 404
2. `IllegalArgumentException` → 400
3. `MethodArgumentNotValidException` → 400
4. `Exception` (모든 예외) → 500

**핵심 기능**:
- 예외 감지 및 처리
- ErrorResponse 생성
- HTTP 상태 코드 매핑
- 로깅 (에러 추적용)

---

## 동작 흐름

### 예시: 존재하지 않는 클러스터 조회

```
[1] 클라이언트 요청
    GET /api/news/cluster/invalid-id

[2] Controller 실행
    - Service 호출
    - 결과가 비어있음
    - NewsNotFoundException 발생 ← 예외 던짐!

[3] GlobalExceptionHandler 감지
    - @ExceptionHandler(NewsNotFoundException.class) 메서드 실행
    - 로그 출력: "NewsNotFoundException: Cluster not found: invalid-id"
    - ErrorResponse 생성
    - HTTP 404 상태 코드 매핑

[4] 클라이언트 응답
    HTTP/1.1 404 Not Found
    {
      "timestamp": "2024-03-17T12:00:00",
      "status": 404,
      "error": "Not Found",
      "message": "Cluster not found: invalid-id",
      "path": "/api/news/cluster/invalid-id"
    }
```

### 핵심 포인트
- Controller는 예외를 던지기만 함 (try-catch 불필요)
- GlobalExceptionHandler가 자동으로 잡아서 처리
- 일관된 에러 형식으로 변환

---

## 예외별 처리 전략

### 예외 매핑 테이블

| 예외 | 상태 코드 | 의미 | 사용 시나리오 |
|------|-----------|------|---------------|
| `NewsNotFoundException` | 404 | Not Found | 리소스를 찾을 수 없음 |
| `IllegalArgumentException` | 400 | Bad Request | 잘못된 파라미터 |
| `MethodArgumentNotValidException` | 400 | Bad Request | Validation 실패 |
| `Exception` | 500 | Internal Server Error | 예상치 못한 에러 |

---

### 사용 가이드

#### NewsNotFoundException (404)
**언제 사용?**
- 존재하지 않는 리소스 조회
- 삭제된 데이터 접근
- 유효하지 않은 ID

**던지는 위치**:
- Service 계층 (권장)
- Controller 계층

**메시지 작성 원칙**:
- 클라이언트가 이해할 수 있는 문구
- 구체적인 리소스 정보 포함
- 예: "Cluster not found: {clusterId}"

---

#### IllegalArgumentException (400)
**언제 사용?**
- 잘못된 카테고리
- 범위를 벗어난 값
- 형식이 맞지 않는 입력

**예시 상황**:
- "INVALID_CATEGORY" 같은 존재하지 않는 카테고리
- 음수 페이지 번호
- 빈 문자열

---

#### MethodArgumentNotValidException (400)
**언제 발생?**
- @Valid 어노테이션과 함께 사용
- DTO Validation 실패 시 자동 발생

**Validation 어노테이션**:
- `@NotBlank`: 빈 문자열 방지
- `@NotNull`: null 방지
- `@Size`: 길이 제한
- `@Pattern`: 정규식 검증

---

#### Exception (500)
**언제 처리?**
- 예상치 못한 모든 예외
- 프로그래밍 오류
- 시스템 장애

**처리 원칙**:
- 상세 에러 정보는 서버 로그에만 기록
- 클라이언트에는 일반적인 메시지만 전달
- 보안상 스택 트레이스 노출 금지

---

## 왜 필요한가

### 1. 클라이언트 경험 개선

**예외 처리 없음**:
```
에러 메시지: "Index 0 out of bounds for length 0"
상태 코드: 500
→ 사용자: "무슨 의미인지 모르겠음"
```

**예외 처리 있음**:
```
에러 메시지: "Cluster not found: invalid-id"
상태 코드: 404
→ 사용자: "아, 없는 클러스터구나"
```

---

### 2. 코드 중복 제거

**분산 처리 (나쁨)**:
- 각 Controller마다 try-catch 반복
- 에러 처리 로직 중복
- 일관성 깨지기 쉬움

**중앙 집중식 (좋음)**:
- GlobalExceptionHandler만 수정
- 모든 API에 자동 적용
- Controller는 비즈니스 로직에만 집중

---

### 3. 프론트엔드 개발 편의

**일관된 에러 형식**:
```javascript
// 한 번만 작성하면 모든 API에 적용!
if (!response.ok) {
  showError(data.message);  // 항상 존재
  
  if (data.status === 404) {
    redirectToNotFound();
  } else if (data.status === 400) {
    showValidationErrors(data.message);
  }
}
```

---

### 4. 디버깅 효율

**로그 예시**:
```
2024-03-17 12:00:00 ERROR NewsNotFoundException: Cluster not found: invalid-id
```

**효과**:
- 어떤 리소스가 문제인지 즉시 파악
- timestamp로 발생 시점 추적
- path로 API 엔드포인트 확인
- 재현 가능

---

### 5. 유지보수성

**에러 형식 변경 시**:
- ErrorResponse 클래스만 수정
- 모든 API에 자동 반영
- Controller 수정 불필요

**새로운 예외 추가 시**:
- 커스텀 예외 클래스 생성
- GlobalExceptionHandler에 핸들러 추가
- 즉시 적용

---

## 구현 체크리스트

### 파일 생성
- [ ] `exception/ErrorResponse.java` 생성
- [ ] `exception/NewsNotFoundException.java` 생성
- [ ] `exception/GlobalExceptionHandler.java` 생성

### 기능 구현
- [ ] ErrorResponse 필드 5개 (timestamp, status, error, message, path)
- [ ] ErrorResponse.of() 팩토리 메서드
- [ ] NewsNotFoundException extends RuntimeException
- [ ] GlobalExceptionHandler @RestControllerAdvice
- [ ] @ExceptionHandler 4개 (NewsNotFound, IllegalArgument, Validation, Exception)
- [ ] 각 핸들러에 로깅 추가
- [ ] HTTP 상태 코드 매핑

### 테스트
- [ ] 존재하지 않는 리소스 조회 → 404 확인
- [ ] 잘못된 파라미터 → 400 확인
- [ ] Validation 실패 → 400 확인
- [ ] 에러 응답 형식 확인 (5개 필드 모두 포함)
- [ ] 로그 출력 확인

---

## 추가 예외 추가 방법

### 3단계 프로세스

**1단계**: 커스텀 예외 생성
```
exception/DuplicateNewsException.java
→ extends RuntimeException
```

**2단계**: GlobalExceptionHandler에 핸들러 추가
```
@ExceptionHandler(DuplicateNewsException.class)
→ 409 Conflict 반환
```

**3단계**: 사용
```
Controller 또는 Service에서
→ throw new DuplicateNewsException("...");
```

---

## 핵심 개념 요약

| 구성 요소 | 역할 | 핵심 특징 |
|-----------|------|-----------|
| **ErrorResponse** | 에러 응답 표준화 | 일관된 JSON, 5개 필드 |
| **NewsNotFoundException** | 커스텀 예외 | 명확한 의미, RuntimeException |
| **GlobalExceptionHandler** | 중앙 예외 처리 | @RestControllerAdvice, 코드 중복 제거 |

### 설계 원칙
```
✓ 일관성: 모든 에러가 동일한 형식
✓ 명확성: 의미 있는 예외 클래스명
✓ 집중화: 한 곳에서 모든 예외 관리
✓ 분리: 비즈니스 로직과 예외 처리 분리
```

### 장점
```
✓ 클라이언트 친화적
✓ 코드 중복 제거
✓ 디버깅 용이
✓ 유지보수 쉬움
✓ 테스트 가능
```

---