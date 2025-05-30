package com.example.awslocationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Validates AWS Location Service configuration during application startup.
 * <p>
 * This component executes a test call to AWS Location Service when the application
 * starts to verify that the API key and configuration are working correctly.
 * </p>
 */
@Component
@Slf4j
public class AWSLocationServiceValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final String VALIDATION_FAILED = "❌ AWS Location Service API key validation FAILED!";    
    private final AWSLocationProperties awsLocationProperties;
    private final RestTemplate restTemplate;

    public AWSLocationServiceValidator(AWSLocationProperties awsLocationProperties, RestTemplate restTemplate) {
        this.awsLocationProperties = awsLocationProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        validateAwsLocationServiceConfig();
    }

    /**
     * Validates the AWS Location Service configuration by making a test API call.
     * Logs detailed information about success or failure.
     */
    private void validateAwsLocationServiceConfig() {
        log.info("Validating AWS Location Service configuration...");
        
        try {
            // Create test URL
            String url = String.format(
                    "https://places.geo.%s.amazonaws.com/places/v0/indexes/%s/search/text",
                    awsLocationProperties.getRegion(),
                    awsLocationProperties.getPlaceIndexName()
            );
            
            log.info("Testing connection to: {}", url);
            
            // Set up headers with API Key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", awsLocationProperties.getApiKey());
            
            // Create a minimal test request with required data provider
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("Text", "90210");  // Famous Beverly Hills ZIP code
            requestBody.put("MaxResults", 1);  // Only need one result for testing
            requestBody.put("DataSource", awsLocationProperties.getDataProvider());  // Include data provider
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Execute the request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // Check if request was successful
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ AWS Location Service API key validation SUCCESSFUL!");
                log.info("Connection to AWS Location Service working properly.");
            } else {
                log.error(VALIDATION_FAILED);
                log.error("Received non-OK response: {}", response.getStatusCode());
                log.error("Response body: {}", response.getBody());
                logConfigurationHints();
            }
        } catch (RestClientException e) {
            log.error(VALIDATION_FAILED);
            log.error("Failed to connect to AWS Location Service: {}", e.getMessage());
            logConfigurationHints();
        } catch (Exception e) {
            log.error(VALIDATION_FAILED);
            log.error("Unexpected error during validation: {}", e.getMessage(), e);
            logConfigurationHints();
        }
    }
    
    /**
     * Logs helpful hints for fixing configuration issues.
     */
    private void logConfigurationHints() {
        log.error("=== CONFIGURATION HINTS ===");
        log.error("1. Check that aws.location.api-key in application.properties is correctly set");
        log.error("2. Verify that aws.location.place-index-name='{}' exists in your AWS account", 
                awsLocationProperties.getPlaceIndexName());
        log.error("3. Ensure the API key has permissions to access the place index");
        log.error("4. Confirm that aws.location.region='{}' is correct", 
                awsLocationProperties.getRegion());
        log.error("5. The application will continue to run, but address lookup will not work until this is fixed");
    }
}
