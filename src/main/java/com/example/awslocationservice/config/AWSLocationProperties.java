package com.example.awslocationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * Configuration properties for AWS Location Service.
 * <p>
 * This class provides a type-safe way to access AWS Location Service
 * configuration properties defined in application.properties or 
 * environment variables. It validates that required properties are present.
 * </p>
 * 
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@ConfigurationProperties(prefix = "aws.location")
@Data
@Validated
public class AWSLocationProperties {

    /**
     * The API key for authenticating with AWS Location Service.
     */
    @NotBlank
    private String apiKey;
    
    /**
     * The name of the Place Index resource in AWS Location Service.
     */
    @NotBlank
    private String placeIndexName;
    
    /**
     * The AWS region where the AWS Location Service resources are located.
     */
    @NotBlank
    private String region;
    
    /**
     * The name of the Map resource in AWS Location Service.
     */
    @NotBlank
    private String mapName;
}
