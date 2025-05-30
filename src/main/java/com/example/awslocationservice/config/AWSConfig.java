package com.example.awslocationservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.location.AmazonLocation;
import com.amazonaws.services.location.AmazonLocationClient;
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
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                credentialsProperties.getAccessKey(),
                credentialsProperties.getSecretKey()
        );

        return AmazonLocationClient.builder()
                .withRegion(locationProperties.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
