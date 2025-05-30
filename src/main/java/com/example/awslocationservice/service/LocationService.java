package com.example.awslocationservice.service;

import com.example.awslocationservice.config.AWSLocationProperties;
import com.example.awslocationservice.model.AddressResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for interacting with AWS Location Service to validate ZIP codes and retrieve address information.
 * <p>
 * This service provides methods to validate US ZIP code formats and to look up addresses using
 * the AWS Location Service REST API with the HERE provider. It implements API key authentication
 * and handles all necessary HTTP communication and response parsing.
 * </p>
 * 
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@Service
@Slf4j
public class LocationService {

    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_NOT_FOUND = "NOT_FOUND";

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:-\\d{4})?$");
    private static final String AWS_LOCATION_API_URL_TEMPLATE = 
            "https://places.geo.{region}.amazonaws.com/places/v0/indexes/{placeIndexName}/search/text";

    private final AWSLocationProperties awsLocationProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor that injects the required dependencies.
     * 
     * @param awsLocationProperties Typed configuration properties for AWS Location Service
     * @param restTemplate RestTemplate for making HTTP requests
     * @param objectMapper ObjectMapper for JSON processing
     */
    public LocationService(
            AWSLocationProperties awsLocationProperties,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.awsLocationProperties = awsLocationProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Validates if the provided string is a valid US ZIP code format.
     * <p>
     * This method checks if the provided string matches the pattern for US ZIP codes,
     * which can be either a 5-digit format (e.g., "92021") or a 9-digit format with
     * hyphen (e.g., "92021-1234").
     * </p>
     * 
     * @param zipCode The ZIP code string to validate
     * @return {@code true} if the ZIP code is valid, {@code false} otherwise
     * @throws NullPointerException if zipCode is null
     */
    public boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Looks up address information for a provided ZIP code using AWS Location Service.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Validates the ZIP code format</li>
     *   <li>Constructs an HTTP request to the AWS Location Service API</li>
     *   <li>Sends the request with proper API key authentication</li>
     *   <li>Processes the response and extracts address information</li>
     *   <li>Returns a structured result with address details and coordinates</li>
     * </ol>
     * </p>
     * 
     * @param zipCode The ZIP code to look up (e.g., "92021")
     * @return {@link AddressResult} object containing address information and coordinates
     *         or error details if the operation failed
     * @see AddressResult
     */
    public AddressResult lookupAddressByZipCode(String zipCode) {
        if (!isValidZipCode(zipCode)) {
            log.error("Invalid ZIP code format: {}", zipCode);
            return AddressResult.builder()
                    .status(STATUS_ERROR)
                    .errorMessage("Invalid ZIP code format")
                    .build();
        }

        try {
            // Construct URL without API key in query string
            String url = AWS_LOCATION_API_URL_TEMPLATE
                    .replace("{region}", awsLocationProperties.getRegion())
                    .replace("{placeIndexName}", awsLocationProperties.getPlaceIndexName());

            log.debug("Using URL: {}", url);
            
            // Set up headers with API Key using the standard header for AWS Location Service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", awsLocationProperties.getApiKey());
            
            // Create the simplest possible request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("Text", zipCode);
            requestBody.put("MaxResults", 5);
            
            // Simplifying the request by removing all optional parameters
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Sending request to AWS Location Service with ZIP: {}", zipCode);
            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            log.debug("Response status code: {}", response.getStatusCode());
            log.debug("Response body: {}", response.getBody());
        
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Received successful response from AWS Location Service");
                return parseResponse(response.getBody());
            } else {
                log.error("Error from AWS Location Service: Status code {}", response.getStatusCode());
                return AddressResult.builder()
                        .status(STATUS_ERROR)
                        .errorMessage("Error response from AWS Location Service: " + response.getStatusCode())
                        .build();
            }
        } catch (RestClientException e) {
            log.error("Error calling AWS Location Service: {}", e.getMessage(), e);
            return AddressResult.builder()
                    .status(STATUS_ERROR)
                    .errorMessage("Error calling AWS Location Service: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during address lookup", e);
            return AddressResult.builder()
                    .status(STATUS_ERROR)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Parses the JSON response from AWS Location Service into our AddressResult model.
     * <p>
     * This method handles the extraction of address components, coordinates, and other
     * relevant information from the AWS Location Service response format. It processes
     * the structured JSON data and maps it to our internal {@link AddressResult} model.
     * </p>
     * 
     * @param responseBody The JSON response body as a string
     * @return {@link AddressResult} object populated with data from the response,
     *         or with error information if parsing fails
     * @throws Exception if JSON parsing or data extraction fails
     */
    private AddressResult parseResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultsNode = rootNode.path("Results");
            
            if (resultsNode.isEmpty()) {
                return AddressResult.builder()
                        .status(STATUS_NOT_FOUND)
                        .errorMessage("No addresses found for the provided ZIP code")
                        .build();
            }
            
            List<AddressResult.Place> places = new ArrayList<>();
            double latitude = 0;
            double longitude = 0;
            String formattedAddress = "";
            
            for (JsonNode resultNode : resultsNode) {
                JsonNode placeNode = resultNode.path("Place");
                JsonNode addressNode = placeNode.path("Address");
                
                AddressResult.Place place = AddressResult.Place.builder()
                        .addressNumber(addressNode.path("AddressNumber").asText(""))
                        .street(addressNode.path("Street").asText(""))
                        .municipality(addressNode.path("Municipality").asText(""))
                        .region(addressNode.path("Region").asText(""))
                        .country(addressNode.path("Country").asText(""))
                        .postalCode(addressNode.path("PostalCode").asText(""))
                        .label(placeNode.path("Label").asText(""))
                        .build();
                
                places.add(place);
                
                // Get the coordinates from the first result
                if (latitude == 0 && longitude == 0) {
                    JsonNode positionNode = placeNode.path("Geometry").path("Point");
                    if (positionNode.isArray() && positionNode.size() >= 2) {
                        longitude = positionNode.get(0).asDouble();
                        latitude = positionNode.get(1).asDouble();
                    }
                    
                    formattedAddress = place.getLabel();
                }
            }
            
            return AddressResult.builder()
                    .status(STATUS_SUCCESS)
                    .places(places)
                    .formattedAddress(formattedAddress)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            
        } catch (Exception e) {
            log.error("Error parsing AWS Location Service response", e);
            return AddressResult.builder()
                    .status(STATUS_ERROR)
                    .errorMessage("Error parsing response: " + e.getMessage())
                    .build();
        }
    }
}
