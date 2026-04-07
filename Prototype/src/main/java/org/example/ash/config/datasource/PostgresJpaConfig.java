package org.example.ash.config.datasource;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.ash.config.datasource.annotation.MultiDataSource;
import org.example.ash.config.datasource.config.PostgresConnectionPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.sql.SQLException;

@MultiDataSource(
        propertiesPrefix      = "custom.multi-databases.postgres",
        poolConfigPrefix      = "datasource.pool.postgres",
        dataSourceBeanName    = "postgresDataSource",
        entityManagerFactoryBeanName = "postgresEntityManagerFactory",
        transactionManagerBeanName   = "postgresTransactionManager",
        entityPackages        = {"org.example.ash.entity.postgres"},
        repositoryPackages    = {"org.example.ash.repository.postgres"},
        poolName              = "postgres-pool",
        databaseType          = MultiDataSource.DatabaseType.POSTGRESQL,
        primary               = false
)
@EnableConfigurationProperties(PostgresConnectionPoolConfig.class)
@EnableJpaRepositories(
        basePackages            = "org.example.ash.repository.postgres",
        entityManagerFactoryRef = "postgresEntityManagerFactory",
        transactionManagerRef   = "postgresTransactionManager"
)
@RequiredArgsConstructor
public class PostgresJpaConfig extends BaseJpaConfig {

    private final PostgresConnectionPoolConfig poolConfig;

    // ── Connection properties (url / username / password / driver) ─────────────
    @Bean
    @ConfigurationProperties("custom.multi-databases.postgres")
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    // ── DataSource ─────────────────────────────────────────────────────────────
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource() throws SQLException {
        return buildDataSource(postgresDataSourceProperties(), poolConfig);
    }

    // ── EntityManagerFactory ───────────────────────────────────────────────────
    @Bean(name = "postgresEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder) throws SQLException {
        return buildEntityManagerFactory(builder, postgresDataSource(), "postgres");
    }

    // ── TransactionManager ─────────────────────────────────────────────────────
    @Bean(name = "postgresTransactionManager")
    public JpaTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory") EntityManagerFactory emf) {
        return buildTransactionManager(emf);
    }
}