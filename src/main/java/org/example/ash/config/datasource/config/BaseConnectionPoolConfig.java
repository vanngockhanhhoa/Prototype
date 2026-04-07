package org.example.ash.config.datasource.config;

import lombok.Data;

@Data
public abstract class BaseConnectionPoolConfig {
    private Long connectionTimeout;
    private Long idleTimeout;
    private Long maxLifeTime;
}

