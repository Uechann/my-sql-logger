# 06. 테스트 — Spring Boot Test로 Auto-configuration 검증

## 목표
라이브러리 JAR를 사용하는 측과 동일한 조건으로 Spring 컨텍스트를 띄워 검증한다.

## 테스트 설정 방법

라이브러리 프로젝트에는 `@SpringBootApplication` 클래스가 없다.
`@SpringBootTest`가 스캔할 루트 클래스를 직접 지정해야 한다.

```java
@SpringBootTest(classes = SqlLoggerAutoConfigurationTest.TestConfig.class)
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,    // DataSource 빈 생성
        JdbcTemplateAutoConfiguration.class,  // JdbcTemplate 빈 생성
        SqlLoggerAutoConfiguration.class      // 우리 Auto-configuration
})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        ...
})
class SqlLoggerAutoConfigurationTest {

    @Configuration
    static class TestConfig {}  // 비어있어도 됨 — @SpringBootTest의 루트 클래스 역할

    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbcTemplate;
}
```

## 검증 포인트

### 1. DataSource가 ProxyDataSource로 감싸졌는가

```java
@Test
void dataSourceShouldBeWrappedWithProxy() {
    assertThat(dataSource).isInstanceOf(ProxyDataSource.class);
}
```

### 2. 실제 쿼리 실행 시 로그가 출력되는가

```java
@Test
void shouldExecuteQueryWithLogging() {
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_log (id INT, name VARCHAR(50))");
    jdbcTemplate.update("INSERT INTO test_log VALUES (?, ?)", 1, "hello");

    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_log", Integer.class);
    assertThat(count).isEqualTo(1);
}
```

로그 출력은 테스트 콘솔에서 직접 확인:
```
INFO SQL : [SQL] 0ms | INSERT INTO test_log VALUES (1, 'hello')
```

## 트러블슈팅

### useJUnitPlatform() 누락 → 0 tests, BUILD SUCCESSFUL
Gradle 기본 테스트 엔진은 JUnit 4. `build.gradle`에 반드시 추가:
```groovy
test {
    useJUnitPlatform()
}
```

### @SpringBootTest(classes = {}) → NoSuchBeanDefinitionException
빈 배열을 넘기면 Spring이 클래스를 스캔하지 못해 DataSource 빈이 없다.
`TestConfig` 내부 클래스를 만들고 `classes = TestConfig.class`로 지정한다.

### DB_CLOSE_DELAY=-1
H2 인메모리 DB는 connection이 닫히면 즉시 DB가 사라진다.
여러 테스트가 같은 DB를 공유하려면 `DB_CLOSE_DELAY=-1`로 유지시켜야 한다.
