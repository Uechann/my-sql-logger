# my-sql-logger

**Spring Boot에서 `?` 없이 실제 파라미터가 치환된 SQL을 로깅하는 라이브러리입니다.**

`build.gradle`에 한 줄 추가하면 설정 없이 바로 동작합니다.

```
INFO SQL :
┌──────────────────────────────────────────────────────────────────┐
│ SQL                                                              │
├────────────┬─────────────────────────────────────────────────────┤
│ time       │ 2026-06-03 18:15:42.123                             │
│ connection │ conn-4a3b2c1d                                       │
│ elapsed    │ 3ms                                                 │
├────────────┼─────────────────────────────────────────────────────┤
│ query      │ SELECT *                                            │
│            │   FROM users                                        │
│            │  WHERE id = 1                                       │
│            │    AND name = 'alice'                               │
└────────────┴─────────────────────────────────────────────────────┘
```

---

## 시작하기

### 1. 의존성 추가

```groovy
// build.gradle
repositories {
    maven { url 'https://jitpack.io' }
    mavenCentral()
}

dependencies {
    implementation 'com.github.Uechann:my-sql-logger:v1.0.0'
}
```

### 2. 끝

별도 설정 없이 모든 SQL이 로깅됩니다.

---

## 기존 방식과의 차이

일반적인 Spring Boot SQL 로그는 파라미터가 `?`로 표시됩니다.

```
# 기존 (JPA, MyBatis 기본 로그)
Hibernate: select u1_0.id, u1_0.name from users u1_0 where u1_0.id=?

# my-sql-logger (table 포맷)
┌──────────────────────────────────────────────────────────────────┐
│ SQL                                                              │
├────────────┬─────────────────────────────────────────────────────┤
│ time       │ 2026-06-03 18:15:42.123                             │
│ connection │ conn-4a3b2c1d                                       │
│ elapsed    │ 3ms                                                 │
├────────────┼─────────────────────────────────────────────────────┤
│ query      │ SELECT u1_0.id,                                     │
│            │        u1_0.name                                    │
│            │   FROM users u1_0                                   │
│            │  WHERE u1_0.id = 1                                  │
└────────────┴─────────────────────────────────────────────────────┘
```

파라미터가 치환된 완성된 SQL을 바로 복사해서 DB 클라이언트에 실행할 수 있습니다.

---

## 설정

`application.properties`에서 원하는 항목만 추가합니다. 모든 값은 기본값으로 동작하므로 설정을 생략해도 됩니다.

```properties
# 전체 on/off (기본값: true)
sql-logger.enabled=true

# 로그 포맷 (기본값: table)
# single_line | multi_line | json | table
sql-logger.format=table

# 느린 쿼리만 출력 — 이 값(ms) 초과 쿼리만 로깅 (기본값: -1, 모두 출력)
sql-logger.slow-query-threshold-ms=100

# 실행 시간 표시 (기본값: true)
sql-logger.show-execution-time=true

# 파라미터 치환 여부 (기본값: true)
sql-logger.show-parameters=true
```

### 로그 포맷 예시

**`table`** (기본값) — 실행 시각, connection, elapsed, 정렬된 SQL을 표로 출력
```
┌──────────────────────────────────────────────────────────────────┐
│ SQL                                                              │
├────────────┬─────────────────────────────────────────────────────┤
│ time       │ 2026-06-03 18:15:42.123                             │
│ connection │ conn-4a3b2c1d                                       │
│ elapsed    │ 3ms                                                 │
├────────────┼─────────────────────────────────────────────────────┤
│ query      │ INSERT INTO orders                                  │
│            │ VALUES (42, 'coffee', 4500)                         │
└────────────┴─────────────────────────────────────────────────────┘
```

**`single_line`**
```
[SQL] 3ms | [conn-4a3b2c1d] INSERT INTO orders VALUES (42, 'coffee', 4500)
```

**`multi_line`**
```
[SQL]
time       : 2026-06-03 18:15:42.123
connection : conn-4a3b2c1d
elapsed    : 3ms
query :
INSERT INTO orders
VALUES (42, 'coffee', 4500)
```

**`json`** — ELK Stack, Datadog 등 로그 수집 시스템 연동 시
```json
{"type":"SQL","connection":"conn-4a3b2c1d","elapsed_ms":3,"query":"INSERT INTO orders VALUES (42, 'coffee', 4500)"}
```

---

## SQL 포맷팅 규칙

`table` / `multi_line` 포맷은 SQL을 다음 규칙으로 자동 정렬합니다.

- **키워드 대문자화** — `SELECT`, `FROM`, `WHERE`, `JOIN` 등
- **절(Clause)별 줄 바꿈** — 각 절은 새로운 줄에서 시작
- **오른쪽 정렬** — 주요 키워드를 6자 기준으로 오른쪽 정렬하여 가독성 향상
- **SELECT 컬럼 정렬** — 컬럼 목록을 세로로 가지런히 정렬
- **기존 개행 정규화** — JPA/Hibernate 등에서 이미 개행이 포함된 SQL도 올바르게 재포맷

```sql
-- 입력 (JPA가 생성한 SQL)
select u1_0.id, u1_0.name, u1_0.email
from users u1_0
where u1_0.id = 1 and u1_0.status = 'ACTIVE'

-- 출력
SELECT u1_0.id,
       u1_0.name,
       u1_0.email
  FROM users u1_0
 WHERE u1_0.id = 1
   AND u1_0.status = 'ACTIVE'
```

---

## 포맷터 커스터마이징

`SqlLogFormatter` 빈을 직접 등록하면 기본 포맷터 대신 사용됩니다.

```java
@Bean
public SqlLogFormatter myFormatter() {
    return context -> String.format("[MY-SQL] %dms | [%s] %s",
            context.getElapsedMs(), context.getConnectionId(), context.getSql());
}
```

`SqlLogContext`에서 제공하는 정보:

| 메서드 | 설명 |
|--------|------|
| `getSql()` | 파라미터 치환 완료된 SQL |
| `getElapsedMs()` | 실행 시간 (ms) |
| `getConnectionId()` | 커넥션 식별자 (`conn-{hex}`) |
| `getExecutedAt()` | 실행 시각 (`LocalDateTime`) |
| `isError()` | 에러 여부 (`elapsedMs < 0`) |

---

## 버전

| 버전 | Spring Boot | Java |
|---|---|---|
| v1.0.0 | 3.x | 17+ |

---

## 배포 방법 (JitPack)

1. GitHub에 코드 push
2. GitHub → **Releases** → **Create a new release** → 태그 `v1.0.0` 생성
3. [jitpack.io](https://jitpack.io) 에서 `Uechann/my-sql-logger` 검색 후 빌드 확인
4. 위의 의존성 코드로 바로 사용 가능
