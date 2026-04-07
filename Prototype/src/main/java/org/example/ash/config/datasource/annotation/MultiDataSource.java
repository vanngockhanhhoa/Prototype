package org.example.ash.config.datasource.annotation;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Configuration
public @interface MultiDataSource {
    String propertiesPrefix();
    String dataSourceBeanName();
    String entityManagerFactoryBeanName();
    String transactionManagerBeanName();
    String[] entityPackages();
    String[] repositoryPackages();
    String poolName();
    String schemaPropertyKey() default "";
    DatabaseType databaseType() default DatabaseType.ORACLE;
    String poolConfigPrefix() default "";

    boolean primary() default false;

    enum DatabaseType {
        POSTGRESQL,
        ORACLE,
        MYSQL,
        H2
    }
}

