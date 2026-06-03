# 04. Proxy 계층 — DataSource → Connection → PreparedStatement

## 목표
JDBC 호출을 가로채서 SQL과 파라미터를 기록한다.
사용자는 JDBC URL이나 드라이버 클래스명을 변경할 필요가 없다.

## 구조

```
DataSource.getConnection()
    → ProxyConnection 반환
        → prepareStatement(sql)
            → ProxyPreparedStatement 반환
                → setString(), setLong(), ... 호출 시 파라미터 기록
                → executeQuery() / executeUpdate() 호출 시 시간 측정 + 로그 출력
```

## P6Spy 방식과의 차이

| | P6Spy | 이 라이브러리 |
|---|---|---|
| 방식 | JDBC Driver Proxy | DataSource BeanPostProcessor |
| 설정 변경 | URL에 `p6spy:` 추가 필요 | 불필요 |
| Spring 통합 | 수동 | 자동 |

## ProxyPreparedStatement의 파라미터 기록

```java
private final TreeMap<Integer, String> parameters = new TreeMap<>();

@Override
public void setString(int parameterIndex, String x) throws SQLException {
    if (properties.isShowParameters()) {
        parameters.put(parameterIndex, x == null ? "NULL" : "'" + x + "'");
    }
    delegate.setString(parameterIndex, x);
}
```

`TreeMap`을 쓰는 이유: 파라미터 인덱스(1, 2, 3...)를 정렬된 순서로 유지해야 `?`를 순서대로 치환할 수 있기 때문이다.

## fillParameters — ? 치환 로직

```java
private String fillParameters(String sql) {
    StringBuilder result = new StringBuilder(sql);
    for (String value : parameters.values()) {
        int idx = result.indexOf("?");
        if (idx == -1) break;
        result.replace(idx, idx + 1, value);
    }
    return result.toString();
}
```

순서대로 첫 번째 `?`를 찾아 값으로 교체한다.
`parameters.values()`는 TreeMap이므로 인덱스 오름차순으로 반환된다.

## 느린 쿼리 필터링

```java
if (properties.getSlowQueryThresholdMs() >= 0
        && elapsedMs >= 0
        && elapsedMs < properties.getSlowQueryThresholdMs()) {
    return; // 임계값 미만이면 출력 안 함
}
```

- `slowQueryThresholdMs < 0` (기본값 -1): 필터링 없이 모든 쿼리 출력
- `slowQueryThresholdMs >= 0`: 해당 ms 이상인 쿼리만 출력
- `elapsedMs < 0` (오류): 느린 쿼리 필터링과 무관하게 항상 출력

## PreparedStatement 위임

`PreparedStatement`는 메서드가 50개가 넘는다. 기록이 필요한 메서드(`setXxx`, `executeXxx`)만 직접 구현하고 나머지는 `delegate`에 위임한다.

```java
@Override
public void close() throws SQLException { delegate.close(); }

@Override
public int getMaxRows() throws SQLException { return delegate.getMaxRows(); }
// ...
```

`setUnicodeStream`은 Java 1.1부터 deprecated된 메서드인데 `PreparedStatement` 인터페이스에 여전히 포함되어 있어서 구현해야 한다. 이 때문에 컴파일 시 deprecated API 경고가 발생한다.
