package org.hit.chiikaiwabe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseSetupConfig {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void setupDatabaseIndexes() {
        log.info("Starting advanced database index setup...");
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm;");
            log.info("Extension pg_trgm is ready.");

            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_message_content_trgm ON messages USING gin (content gin_trgm_ops);");

            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_conversation_name_trgm ON conversations USING gin (group_name gin_trgm_ops);");

            log.info("Advanced database indexes have been successfully created/verified.");
        } catch (Exception e) {
            log.warn("Could not create advanced database indexes. Error: {}", e.getMessage());
        }
    }
}
