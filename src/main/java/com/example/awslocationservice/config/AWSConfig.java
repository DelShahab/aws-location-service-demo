package com.example.awslocationservice.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.location.AmazonLocation;
import com.amazonaws.services.location.AmazonLocationClient;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for AWS services.
 * <p>
 * This class configures AWS clients using credentials from application properties.
 * </p>
 *
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@Configuration
@Slf4j
public class AWSConfig {

    private final AWSCredentialsProperties credentialsProperties;
    private final AWSLocationProperties locationProperties;

    public AWSConfig(AWSCredentialsProperties credentialsProperties, AWSLocationProperties locationProperties) {
        this.credentialsProperties = credentialsProperties;
        this.locationProperties = locationProperties;
    }

    /**
     * Creates and configures an AWS Location Service client.
     *
     * @return Configured AmazonLocation client
     */
    @Bean
    public AmazonLocation amazonLocationClient() {
        String accessKey = credentialsProperties.getAccessKey();
        String secretKey = credentialsProperties.getSecretKey();
        String region = locationProperties.getRegion();
        
        // Log credential information (partially masked for security)
        if (accessKey != null && !accessKey.isEmpty()) {
            String maskedKey = accessKey.substring(0, Math.min(4, accessKey.length())) + "****";
            log.info("Configuring AWS client with access key: {}", maskedKey);
        } else {
            log.warn("AWS access key is empty or null");
        }
        
        if (secretKey == null || secretKey.isEmpty()) {
            log.warn("AWS secret key is empty or null");
        }
        
        log.info("AWS Location Service region: {}", region);
        
        // Create AWS credentials - using session credentials if session token is available
        AWSCredentials awsCredentials;
        String sessionToken = credentialsProperties.getSessionToken();
        
        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
            log.info("Using session token for temporary credentials");
            awsCredentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);
        } else {
            log.info("Using long-term credentials (no session token)");
            awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        }
        
        // Configure client with longer timeouts and retry policy
        ClientConfiguration clientConfig = new ClientConfiguration()
                .withConnectionTimeout(10000)  // 10 seconds
                .withSocketTimeout(10000)      // 10 seconds
                .withMaxErrorRetry(3);         // 3 retries
                
        // Convert string region to Regions enum
        Regions awsRegion = getAwsRegion(region);
        
        try {
            // Build and return the client
            return AmazonLocationClient.builder()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(clientConfig)
                    .withRegion(awsRegion)
                    .build();
        } catch (Exception e) {
            log.error("Error creating AWS Location client", e);
            throw new IllegalStateException("Failed to create AWS Location client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a region string to AWS Regions enum.
     * Falls back to US_WEST_2 if the region is invalid.
     * 
     * @param region Region string to convert
     * @return AWS Regions enum
     */
    private Regions getAwsRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            log.warn("Region is null or empty, using default region US_WEST_2");
            return Regions.US_WEST_2;
        }
        
        try {
            Regions awsRegion = Regions.fromName(region);
            log.info("Using AWS region: {}", awsRegion);
            return awsRegion;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid region name: {}, falling back to us-west-2", region);
            return Regions.US_WEST_2;
        }
    }
}
