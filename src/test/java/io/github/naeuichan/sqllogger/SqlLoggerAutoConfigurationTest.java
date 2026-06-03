package io.github.naeuichan.sqllogger;

import io.github.naeuichan.sqllogger.proxy.ProxyDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SqlLoggerAutoConfigurationTest.TestConfig.class)
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        SqlLoggerAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "sql-logger.enabled=true",
        "sql-logger.format=SINGLE_LINE"
})
class SqlLoggerAutoConfigurationTest {

    @Configuration
    static class TestConfig {
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void dataSourceShouldBeWrappedWithProxy() {
        assertThat(dataSource).isInstanceOf(ProxyDataSource.class);
    }

    @Test
    void shouldExecuteQueryWithLogging() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_log (id INT, name VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO test_log VALUES (?, ?)", 1, "hello");

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_log", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
