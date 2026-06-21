package com.nector.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class KpiConfig {
    // Configuration for KRA/KPI module
    // Scheduling is enabled for automatic expiration of old assignments
}
