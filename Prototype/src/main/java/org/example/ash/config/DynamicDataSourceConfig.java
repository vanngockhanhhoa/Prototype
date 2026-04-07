//package org.example.ash.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.apache.logging.log4j.util.Strings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Configuration
//public class DynamicDataSourceConfig {
//
//    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceConfig.class);
//
//    private final DatabaseProperties databaseProperties;
//    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
//    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
//
//
//    public DynamicDataSourceConfig(DatabaseProperties databaseProperties) {
//        this.databaseProperties = databaseProperties;
//    }
//
//    public JdbcTemplate getJdbcTemplate(String name) {
//        return getJdbcTemplate(name, Strings.EMPTY);
//    }
//
//    public JdbcTemplate getJdbcTemplate(String name, String checkerName) {
//        String keyName = name + ":" + checkerName;
//        return jdbcTemplates.computeIfAbsent(keyName, k -> {
//            DatabaseProperties.DbConfig config = databaseProperties.getMultiDatabases().get(name);
//            if (config == null) {
//                throw new IllegalArgumentException("Cannot find database config for key: " + k);
//            }
//            DataSource ds = dataSources.computeIfAbsent(
//                    k, key -> buildDataSourceWithRetry(config, 3, 2000, checkerName)
//            );
//            return new JdbcTemplate(ds);
//        });
//    }
//
//    private DataSource buildDataSourceWithRetry(DatabaseProperties.DbConfig config, int maxRetries, long delayMillis, String checkerName) {
//        SQLException lastException = null;
//        for (int attempt = 1; attempt <= maxRetries; attempt++) {
//            try {
//                if (Strings.isNotBlank(checkerName)) {
//                    config.setUrl(config.getUrl() + "|endUserId=" + checkerName);
//                }
//                Class.forName(config.getDriverClassName());
//                HikariDataSource ds = new HikariDataSource();
//                ds.setDriverClassName(config.getDriverClassName());
//                ds.setJdbcUrl(config.getUrl());
//                ds.setUsername(config.getUsername());
//                ds.setPassword(config.getPassword());
//                ds.setMaximumPoolSize(10);
//                ds.setConnectionTimeout(30000);
//
//                try (Connection connection = ds.getConnection()){
//                    log.info("Successfully connected to database: {} on attempt {}", config.getUrl(), attempt);
//                }
//
//                return ds;
//
//            } catch (SQLException | ClassNotFoundException e) {
//                lastException = e instanceof SQLException ? (SQLException) e : null;
//                log.warn("Failed to build datasource for {}. Attempt {}/{}. Cause: {}", config.getUrl(), attempt, maxRetries, e.getMessage());
//                try {
//                    Thread.sleep(delayMillis);
//                } catch (InterruptedException interruptedException) {
//                }
//            }
//        }
//        throw new RuntimeException("Cannot connect to database: " + config.getUrl(), lastException);
//    }
//
//    @PreDestroy
//    public void closeAllPools() {
//        dataSources.values().forEach(
//                ds -> {
//                 if (ds instanceof HikariDataSource hikari) {
//                     log.info("Closing datasource: {}", hikari.getJdbcUrl());
//                     hikari.close();
//                 }
//                }
//        );
//    }
//
//    @PostConstruct
//    public void initDefaultTemplates() {
//        List<String> defaultKeys = List.of("oracle", "postgres");
//        for (String key : defaultKeys) {
//            try {
//                getJdbcTemplate(key);
//                log.info("Initialized default JdbcTemplate for key: {}", key);
//            } catch (Exception e) {
//                log.warn("Failed to init JdbcTemplate for {}: {}", key, e.getMessage());
//            }
//        }
//    }
//
//    @Bean(name = "oracleJdbcTemplate")
//    public JdbcTemplate oracleJdbcTemplate() {
//        return getJdbcTemplate("oracle");
//    }
//
//    @Bean(name = "postgresJdbcTemplate")
//    public JdbcTemplate postgresJdbcTemplate() {
//        return getJdbcTemplate("postgres");
//    }
//}
