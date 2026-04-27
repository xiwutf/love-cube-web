package com.lovecube.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "lovecube.seed", name = "enabled", havingValue = "true")
public class DevSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);
    private static final String SEED_CHECK_SQL = "SELECT COUNT(*) FROM users WHERE openid LIKE 'dev_seed_openid_%'";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DevSeedRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (hasSeedData()) {
            log.info("Dev seed data already exists, skip execution.");
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(new ClassPathResource("db/seed/dev-seed.sql"));
        DatabasePopulatorUtils.execute(populator, dataSource);

        log.info("Dev seed data applied from classpath:db/seed/dev-seed.sql");
    }

    private boolean hasSeedData() {
        try {
            Integer count = jdbcTemplate.queryForObject(SEED_CHECK_SQL, Integer.class);
            return count != null && count > 0;
        } catch (Exception ex) {
            log.warn("Unable to check existing seed data, continue with SQL-level idempotent script.", ex);
            return false;
        }
    }
}
