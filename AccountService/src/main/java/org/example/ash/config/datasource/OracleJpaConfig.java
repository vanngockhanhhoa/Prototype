package org.example.ash.config.datasource;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.ash.config.datasource.annotation.MultiDataSource;
import org.example.ash.config.datasource.config.OracleConnectionPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.sql.SQLException;

@MultiDataSource(
        propertiesPrefix      = "custom.multi-databases.oracle",
        poolConfigPrefix      = "datasource.pool.oracle",
        dataSourceBeanName    = "oracleDataSource",
        entityManagerFactoryBeanName = "oracleEntityManagerFactory",
        transactionManagerBeanName   = "oracleTransactionManager",
        entityPackages        = {"org.example.ash.entity.oracle"},
        repositoryPackages    = {"org.example.ash.repository.oracle"},
        poolName              = "oracle-pool",
        databaseType          = MultiDataSource.DatabaseType.ORACLE,
        primary               = true
)
@EnableConfigurationProperties(OracleConnectionPoolConfig.class)
@EnableJpaRepositories(
        basePackages            = "org.example.ash.repository.oracle",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef   = "oracleTransactionManager"
)
@RequiredArgsConstructor
public class OracleJpaConfig extends BaseJpaConfig {

    private final OracleConnectionPoolConfig poolConfig;

    // ── Connection properties (url / username / password / driver) ─────────────
    @Bean
    @ConfigurationProperties("custom.multi-databases.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    // ── DataSource ─────────────────────────────────────────────────────────────
    @Bean(name = "oracleDataSource")
    @Primary
    public DataSource oracleDataSource() throws SQLException {
        return buildDataSource(oracleDataSourceProperties(), poolConfig);
    }

    // ── EntityManagerFactory ───────────────────────────────────────────────────
    @Bean(name = "oracleEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            EntityManagerFactoryBuilder builder) throws SQLException {
        return buildEntityManagerFactory(builder, oracleDataSource(), "oracle");
    }

    // ── TransactionManager ─────────────────────────────────────────────────────
    @Bean(name = "oracleTransactionManager")
    @Primary
    public JpaTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory emf) {
        return buildTransactionManager(emf);
    }
}