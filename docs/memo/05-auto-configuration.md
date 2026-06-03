# 05. Auto-configuration — Spring Boot가 자동으로 빈을 등록하는 방법

## 목표
사용자가 `@Bean`이나 `@ComponentScan` 없이 의존성 추가만으로 라이브러리가 동작하게 한다.

## 동작 흐름

```
[Spring Boot 시작]
    → classpath 스캔
    → META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 발견
    → 여기 적힌 클래스들을 @Configuration으로 처리
    → @ConditionalOn... 조건 평가
    → 조건에 맞는 빈만 등록
```

## 필수 파일

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
io.github.naeuichan.sqllogger.SqlLoggerAutoConfiguration
```

이 파일이 없으면 아무리 `@AutoConfiguration`을 붙여도 동작하지 않는다.
Spring Boot 2.7 이전은 `META-INF/spring.factories` 파일이었다.

## @ConditionalOn... 조건들

```java
@ConditionalOnClass(DataSource.class)
// → DataSource 클래스가 classpath에 있을 때만 (JDBC 없는 프로젝트에서는 비활성화)

@ConditionalOnProperty(prefix = "sql-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
// → sql-logger.enabled=true이거나 설정이 없을 때 (기본 활성화)

@ConditionalOnMissingBean(SqlLogFormatter.class)
// → SqlLogFormatter 빈이 없을 때만 기본 포맷터 등록
```

## BeanPostProcessor로 DataSource 교체

```java
@Bean
public static BeanPostProcessor sqlLoggerDataSourcePostProcessor(...) {
    return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
                return new ProxyDataSource(dataSource, properties, formatter);
            }
            return bean;
        }
    };
}
```

`!(bean instanceof ProxyDataSource)` 체크: BeanPostProcessor는 모든 빈에 대해 호출되므로
이미 ProxyDataSource인 경우 이중으로 감싸지 않도록 방어한다.

## static BeanPostProcessor의 이유

`BeanPostProcessor`는 다른 빈들보다 먼저 등록되어야 한다.
`@Configuration` 클래스 안의 `@Bean` 메서드가 `static`이 아니면 `@Configuration` 인스턴스 자체가
완전히 초기화되기 전에 `BeanPostProcessor`를 생성해야 해서 문제가 생길 수 있다.

`static`으로 선언하면 `@Configuration` 인스턴스 없이 직접 호출 가능하다.

## ObjectProvider로 BeanPostProcessorChecker 경고 제거

`static BeanPostProcessor`가 다른 빈을 직접 파라미터로 받으면 Spring이 경고를 출력한다:
```
Bean 'sqlLogFormatter' is not eligible for getting processed by all BeanPostProcessors
```

이는 `BeanPostProcessor`를 등록하기 위해 `sqlLogFormatter` 빈을 먼저 만들어야 하는데,
이 시점에 아직 모든 `BeanPostProcessor`가 준비되지 않았기 때문이다.

해결책: `ObjectProvider`로 주입받아 실제 사용 시점(`postProcessAfterInitialization` 호출 시)에 꺼낸다.

```java
public static BeanPostProcessor sqlLoggerDataSourcePostProcessor(
        ObjectProvider<SqlLoggerProperties> propertiesProvider,
        ObjectProvider<SqlLogFormatter> formatterProvider) {
    return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof DataSource dataSource ...) {
                SqlLoggerProperties properties = propertiesProvider.getObject(); // 여기서 꺼냄
                SqlLogFormatter formatter = formatterProvider.getObject();
                return new ProxyDataSource(dataSource, properties, formatter);
            }
            return bean;
        }
    };
}
```
