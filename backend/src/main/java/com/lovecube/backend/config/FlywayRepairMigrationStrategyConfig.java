package com.lovecube.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 3.2 自带的 {@code FlywayProperties} 不绑定 {@code repair-on-migrate}，
 * 仅写在 YAML 里不会触发 Flyway {@code repair()}。此处根据同一配置项在启动迁移前先 repair，
 * 用于修复 {@code flyway_schema_history} 与本地脚本 checksum 不一致（例如 V40）。
 * <p>
 * 对齐成功后请将 {@code spring.flyway.repair-on-migrate} 设为 {@code false}，并删除本策略依赖。
 */
@Configuration
public class FlywayRepairMigrationStrategyConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.flyway", name = "repair-on-migrate", havingValue = "true")
    public FlywayMigrationStrategy flywayRepairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
