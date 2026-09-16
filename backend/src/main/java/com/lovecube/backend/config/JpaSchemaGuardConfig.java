package com.lovecube.backend.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Schema 只由 Flyway 管理。即使外部 YAML / 环境变量把 ddl-auto 设为 update，
 * 也不允许 Hibernate 在启动时 ALTER 现有列（例如把 LONGTEXT 改成 TINYTEXT）。
 */
@Configuration
public class JpaSchemaGuardConfig {

    @Bean
    public HibernatePropertiesCustomizer flywayOwnedSchemaGuard() {
        return props -> {
            props.put("hibernate.hbm2ddl.auto", "none");
            props.put("jakarta.persistence.schema-generation.database.action", "none");
        };
    }
}
