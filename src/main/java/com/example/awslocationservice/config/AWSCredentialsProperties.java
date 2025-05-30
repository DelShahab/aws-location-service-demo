package com.example.awslocationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * Configuration properties for AWS credentials.
 * <p>
 * This class provides a type-safe way to access AWS credentials
 * defined in application.properties or environment variables.
 * </p>
 *
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@ConfigurationProperties(prefix = "aws.credentials")
@Data
@Validated
public class AWSCredentialsProperties {

    /**
     * The AWS access key ID.
     */
    @NotBlank
    private String accessKey;

    /**
     * The AWS secret access key.
     */
    @NotBlank
    private String secretKey;
    
    /**
     * The AWS session token for temporary credentials.
     * <p>
     * This is required when using temporary credentials from AWS STS,
     * IAM roles, or from the AWS Management Console.
     * </p>
     */
    private String sessionToken;
}
