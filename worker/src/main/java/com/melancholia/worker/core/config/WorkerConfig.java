package com.melancholia.worker.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerConfig {

    @Bean
    public String managerAddress(@Value("${manager.host}") String host, @Value("${manager.port}") int port) {
        return String.format("http://%s:%d", host, port);
    }

}
