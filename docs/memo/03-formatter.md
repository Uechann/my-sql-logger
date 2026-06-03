# 03. SqlLogFormatter — 전략 패턴으로 포맷터 설계

## 목표
로그 출력 형식을 사용자가 교체할 수 있도록 인터페이스로 분리한다.

## 인터페이스

```java
public interface SqlLogFormatter {
    String format(String sql, long elapsedMs);
}
```

`elapsedMs`가 `-1`이면 SQL 실행 중 예외가 발생한 경우다.

## 3가지 기본 구현체

### SingleLineSqlLogFormatter (기본값)
```
[SQL] 5ms | SELECT * FROM users WHERE id = 1
[SQL-ERROR] SELECT * FROM users WHERE id = 'invalid
```

### MultiLineSqlLogFormatter
```
[SQL]
time  : 5ms
query : SELECT * FROM users WHERE id = 1
```
긴 쿼리를 읽기 쉽게 여러 줄로 출력한다.

### JsonSqlLogFormatter
```json
{"type":"SQL","elapsed_ms":5,"query":"SELECT * FROM users WHERE id = 1"}
```
ELK Stack, Datadog 같은 로그 수집 시스템에 연동할 때 유용하다.
JSON 특수문자(`"`, `\`, `\n`)를 이스케이프 처리한다.

## 사용자 커스터마이징

`@ConditionalOnMissingBean(SqlLogFormatter.class)` 덕분에 사용자가 직접 빈을 등록하면 기본 포맷터는 등록되지 않는다:

```java
@Bean
public SqlLogFormatter myFormatter() {
    return (sql, elapsedMs) -> "[MY-SQL] " + sql + " (" + elapsedMs + "ms)";
}
```

## 설계 선택: 왜 전략 패턴인가

P6Spy는 포맷터가 고정되어 있어 커스터마이징이 불편하다.
인터페이스 하나로 분리하면:
- 사용자가 자유롭게 교체 가능
- 테스트에서 mock으로 교체 가능
- 새로운 포맷 추가 시 기존 코드 변경 불필요
