package org.example.ash.config.datasource.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "datasource.pool.oracle")
public class OracleConnectionPoolConfig extends BaseConnectionPoolConfig{
}