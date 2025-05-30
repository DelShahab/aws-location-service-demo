package com.example.awslocationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.example.awslocationservice.config.AWSLocationProperties;
import com.example.awslocationservice.config.AWSCredentialsProperties;

/**
 * Main Spring Boot application class for the AWS Location Service demo.
 * <p>
 * This application demonstrates integration with AWS Location Service for
 * address validation using a Vaadin UI. It uses hybrid authentication with
 * both AWS credentials and API key for enhanced security.
 * </p>
 *
 * @author DelShahab
 * @version 1.1
 * @since 2025-05-30
 */
@SpringBootApplication
@EnableConfigurationProperties({AWSLocationProperties.class, AWSCredentialsProperties.class})
public class AwsLocationServiceApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(AwsLocationServiceApplication.class, args);
    }
    
    /**
     * Creates a RestTemplate bean for making HTTP requests to AWS Location Service.
     *
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Creates an ObjectMapper bean for JSON processing.
     *
     * @return Configured ObjectMapper instance for JSON serialization/deserialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
