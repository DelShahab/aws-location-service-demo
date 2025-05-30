package com.example.awslocationservice.config;

import com.amazonaws.services.location.AmazonLocation;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextRequest;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Validates AWS Location Service configuration during application startup.
 * <p>
 * This component executes a test call to AWS Location Service when the application
 * starts to verify that the AWS credentials and configuration are working correctly.
 * </p>
 */
@Component
@Slf4j
public class AWSLocationServiceValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final String VALIDATION_FAILED = "❌ AWS Location Service credentials validation FAILED!";    
    private final AWSLocationProperties awsLocationProperties;
    private final AmazonLocation amazonLocationClient;

    public AWSLocationServiceValidator(AWSLocationProperties awsLocationProperties, AmazonLocation amazonLocationClient) {
        this.awsLocationProperties = awsLocationProperties;
        this.amazonLocationClient = amazonLocationClient;
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
            // Create a test request using the AWS SDK
            SearchPlaceIndexForTextRequest request = new SearchPlaceIndexForTextRequest()
                .withIndexName(awsLocationProperties.getPlaceIndexName())
                .withText("90210") // Famous Beverly Hills ZIP code
                .withMaxResults(1); // Only need one result for testing
            
            log.info("Testing connection to AWS Location Service with place index: {}", 
                    awsLocationProperties.getPlaceIndexName());
            
            // Execute the request using the AWS SDK client
            SearchPlaceIndexForTextResult result = amazonLocationClient.searchPlaceIndexForText(request);
            
            // If we get here, the request was successful
            log.info("✅ AWS Location Service credentials validation SUCCESSFUL!");
            log.info("Connection to AWS Location Service working properly.");
            log.info("Found {} results in response.", 
                    result.getResults() != null ? result.getResults().size() : 0);
            
        } catch (Exception e) {
            log.error(VALIDATION_FAILED);
            log.error("Failed to connect to AWS Location Service: {}", e.getMessage());
            logConfigurationHints();
        }
    }
    
    /**
     * Logs helpful hints for fixing configuration issues.
     */
    private void logConfigurationHints() {
        log.error("=== CONFIGURATION HINTS ===");
        log.error("1. Check that aws.credentials.access-key and aws.credentials.secret-key in application.properties are correctly set");
        log.error("2. Verify that aws.location.place-index-name='{}' exists in your AWS account", 
                awsLocationProperties.getPlaceIndexName());
        log.error("3. Ensure the AWS credentials have permissions to access the place index");
        log.error("4. Confirm that aws.location.region='{}' is correct", 
                awsLocationProperties.getRegion());
        log.error("5. The application will continue to run, but address lookup will not work until this is fixed");
    }
}
