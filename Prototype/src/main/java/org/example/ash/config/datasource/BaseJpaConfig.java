package org.example.ash.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.example.ash.config.datasource.annotation.MultiDataSource;
import org.example.ash.config.datasource.config.BaseConnectionPoolConfig;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base JPA configuration. Subclasses must be annotated with {@link MultiDataSource}
 * to supply datasource metadata (entity packages, pool name, DB type, etc.).
 *
 * <p>Each subclass is responsible for declaring:
 * <ul>
 *   <li>{@code @EnableJpaRepositories} pointing to its repository package</li>
 *   <li>{@code @EnableConfigurationProperties} for its pool-config class</li>
 *   <li>{@code @Bean} methods that delegate to the protected helpers below</li>
 * </ul>
 */
public abstract class BaseJpaConfig {

    // ── Annotation helper ──────────────────────────────────────────────────────

    protected MultiDataSource annotation() {
        // AnnotationUtils.findAnnotation traverses the class hierarchy, so it works
        // even when Spring wraps the subclass in a CGLIB proxy at runtime.
        MultiDataSource ann = AnnotationUtils.findAnnotation(this.getClass(), MultiDataSource.class);
        if (ann == null) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " must be annotated with @MultiDataSource");
        }
        return ann;
    }

    // ── Template methods ───────────────────────────────────────────────────────

    /**
     * Build a HikariDataSource from the given connection properties and pool config.
     * Pool name is taken from {@link MultiDataSource#poolName()}.
     */
    protected DataSource buildDataSource(DataSourceProperties dsProps, BaseConnectionPoolConfig pool) throws SQLException {
        HikariDataSource ds = dsProps.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setConnectionTimeout(pool.getConnectionTimeout());
        ds.setIdleTimeout(pool.getIdleTimeout());
        ds.setMaxLifetime(pool.getMaxLifeTime());
        ds.setPoolName(annotation().poolName());

        if (ds.getConnection() == null) {
            throw new IllegalStateException("Connect to database FAILED!");
        }

        return ds;
    }

    /**
     * Build a JPA {@link LocalContainerEntityManagerFactoryBean}.
     * Entity packages and DB dialect are resolved from {@link MultiDataSource}.
     */
    protected LocalContainerEntityManagerFactoryBean buildEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            String persistenceUnit) {

        MultiDataSource ann = annotation();
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", dialectFor(ann.databaseType()));
        jpaProps.put("hibernate.hbm2ddl.auto", "update");
        jpaProps.put("hibernate.show_sql", "true");
        jpaProps.put("hibernate.format_sql", "true");

        return builder
                .dataSource(dataSource)
                .packages(ann.entityPackages())
                .persistenceUnit(persistenceUnit)
                .properties(jpaProps)
                .build();
    }

    /**
     * Wrap an {@link EntityManagerFactory} in a {@link JpaTransactionManager}.
     */
    protected JpaTransactionManager buildTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // ── Dialect resolution ─────────────────────────────────────────────────────

    protected String dialectFor(MultiDataSource.DatabaseType type) {
        return switch (type) {
            case ORACLE     -> "org.hibernate.dialect.OracleDialect";
            case POSTGRESQL -> "org.hibernate.dialect.PostgreSQLDialect";
            case MYSQL      -> "org.hibernate.dialect.MySQLDialect";
            case H2         -> "org.hibernate.dialect.H2Dialect";
        };
    }
}