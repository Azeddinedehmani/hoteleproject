package com.hotel.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * INFRASTRUCTURE LAYER — JPA configuration.
 * Enables auditing (@CreatedDate, @LastModifiedDate) on entities.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.hotel.infrastructure.persistence.repository")
public class JpaConfig {}