package io.github.naeuichan.sqllogger;

import io.github.naeuichan.sqllogger.formatter.*;
import io.github.naeuichan.sqllogger.proxy.ProxyDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(prefix = "sql-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SqlLoggerProperties.class)
public class SqlLoggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SqlLogFormatter.class)
    public SqlLogFormatter sqlLogFormatter(SqlLoggerProperties properties) {
        return switch (properties.getFormat()) {
            case MULTI_LINE -> new MultiLineSqlLogFormatter();
            case JSON -> new JsonSqlLogFormatter();
            default -> new SingleLineSqlLogFormatter();
        };
    }

    /**
     * static이어야 하는 이유: BeanPostProcessor는 Spring 컨테이너 초기화 초반에 등록되어야 한다.
     * ObjectProvider로 주입받아 실제 사용 시점에 빈을 꺼냄으로써 BeanPostProcessorChecker 경고를 제거한다.
     */
    @Bean
    public static BeanPostProcessor sqlLoggerDataSourcePostProcessor(
            ObjectProvider<SqlLoggerProperties> propertiesProvider,
            ObjectProvider<SqlLogFormatter> formatterProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource dataSource
                        && !(bean instanceof ProxyDataSource)) {
                    SqlLoggerProperties properties = propertiesProvider.getObject();
                    SqlLogFormatter formatter = formatterProvider.getObject();
                    return new ProxyDataSource(dataSource, properties, formatter);
                }
                return bean;
            }
        };
    }
}
