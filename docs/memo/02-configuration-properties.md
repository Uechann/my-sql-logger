# 02. SqlLoggerProperties — 설정값 바인딩

## 목표
`application.properties`에 적은 값을 타입 안전하게 Java 객체로 받는다.

## 구현

```java
@ConfigurationProperties(prefix = "sql-logger")
public class SqlLoggerProperties {
    private boolean enabled = true;
    private long slowQueryThresholdMs = -1;
    private Format format = Format.SINGLE_LINE;
    ...
}
```

## 동작 원리

`@ConfigurationProperties`는 `prefix`에 맞는 프로퍼티를 자동으로 필드에 바인딩한다.

```properties
sql-logger.enabled=true
sql-logger.slow-query-threshold-ms=100  ← kebab-case → camelCase 자동 변환
sql-logger.format=json                  ← Enum 이름 대소문자 무시
```

## 주의사항

`@ConfigurationProperties` 클래스는 단독으로 빈이 등록되지 않는다.
`@EnableConfigurationProperties(SqlLoggerProperties.class)`를 `@Configuration` 클래스에 붙여야 빈으로 등록된다.

## spring-boot-configuration-processor

`annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'`를 추가하면
컴파일 시 `META-INF/spring-configuration-metadata.json`이 생성되어
IDE에서 `application.properties` 자동완성이 동작한다.

## -1 관례

`slowQueryThresholdMs = -1` → "비활성화" 의미.
양수이면 그 값(ms)을 초과하는 쿼리만 출력한다.
이 관례는 P6Spy의 `executionThreshold` 설정 방식과 동일하다.
