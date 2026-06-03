# my-sql-logger

**Spring Boot에서 `?` 없이 실제 파라미터가 치환된 SQL을 로깅하는 라이브러리입니다.**

`build.gradle`에 한 줄 추가하면 설정 없이 바로 동작합니다.

```
INFO SQL : [SQL] 3ms | SELECT * FROM users WHERE id = 1 AND name = 'alice'
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

# my-sql-logger
[SQL] 3ms | SELECT * FROM users WHERE id = 1 AND name = 'alice'
```

파라미터가 치환된 완성된 SQL을 바로 복사해서 DB 클라이언트에 실행할 수 있습니다.

---

## 설정

`application.properties`에서 원하는 항목만 추가합니다. 모든 값은 기본값으로 동작하므로 설정을 생략해도 됩니다.

```properties
# 전체 on/off (기본값: true)
sql-logger.enabled=true

# 로그 포맷 (기본값: single_line)
# single_line | multi_line | json
sql-logger.format=single_line

# 느린 쿼리만 출력 — 이 값(ms) 초과 쿼리만 로깅 (기본값: -1, 모두 출력)
sql-logger.slow-query-threshold-ms=100

# 실행 시간 표시 (기본값: true)
sql-logger.show-execution-time=true

# 파라미터 치환 여부 (기본값: true)
sql-logger.show-parameters=true
```

### 로그 포맷 예시

**`single_line`** (기본값)
```
[SQL] 3ms | INSERT INTO orders VALUES (42, 'coffee', 4500)
```

**`multi_line`**
```
[SQL]
time  : 3ms
query : INSERT INTO orders VALUES (42, 'coffee', 4500)
```

**`json`** — ELK Stack, Datadog 등 로그 수집 시스템 연동 시
```json
{"type":"SQL","elapsed_ms":3,"query":"INSERT INTO orders VALUES (42, 'coffee', 4500)"}
```

---

## 포맷터 커스터마이징

`SqlLogFormatter` 빈을 직접 등록하면 기본 포맷터 대신 사용됩니다.

```java
@Bean
public SqlLogFormatter myFormatter() {
    return (sql, elapsedMs) -> String.format("[MY-SQL] %dms >> %s", elapsedMs, sql);
}
```

```
[MY-SQL] 3ms >> SELECT * FROM users WHERE id = 1
```

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
