package com.example.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the application.
 * Defines and configures beans used throughout the application.
 * This class is responsible for setting up core components like RestTemplate
 * for making HTTP requests to external APIs (OMDb and TMDB).
 * 
 * @see RestTemplate
 */
@Configuration
public class AppConfig {

    /**
     * Creates and configures a RestTemplate bean for making HTTP requests.
     * This bean is used by services to communicate with external movie APIs.
     * 
     * @return A configured RestTemplate instance
     * @see RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
