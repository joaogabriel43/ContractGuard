package br.com.contractguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ContractGuard — Automatic breaking-change detector for OpenAPI contracts.
 *
 * <p>Entry point of the Spring Boot application. All configuration is loaded from
 * {@code application.yml} (and profile-specific overrides like {@code application-local.yml}).
 *
 * <p>Architecture: Clean Architecture + DDD. See {@code CLAUDE.md} for full rules.
 */
@SpringBootApplication
public class ContractGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractGuardApplication.class, args);
    }
}
