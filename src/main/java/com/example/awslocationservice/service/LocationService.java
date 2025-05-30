package com.example.awslocationservice.service;

import com.example.awslocationservice.model.AddressResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LocationService {

    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_NOT_FOUND = "NOT_FOUND";

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:-\\d{4})?$");
    private static final String AWS_LOCATION_API_URL_TEMPLATE = 
            "https://places.geo.{region}.amazonaws.com/places/v0/indexes/{placeIndexName}/search/text";

    private final String apiKey;
    private final String placeIndexName;
    private final String region;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public LocationService(
            @Value("${aws.location.api-key}") String apiKey,
            @Value("${aws.location.place-index-name}") String placeIndexName,
            @Value("${aws.location.region}") String region,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.placeIndexName = placeIndexName;
        this.region = region;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Validates if the provided string is a valid US ZIP code format.
     * 
     * @param zipCode The ZIP code to validate
     * @return true if the ZIP code is valid, false otherwise
     */
    public boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Looks up address information for a provided ZIP code using AWS Location Service.
     * 
     * @param zipCode The ZIP code to look up
     * @return AddressResult object containing address information and coordinates
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
            String url = AWS_LOCATION_API_URL_TEMPLATE
                    .replace("{region}", region)
                    .replace("{placeIndexName}", placeIndexName);

            // Set up headers with API Key - AWS Location Service uses X-Api-Key header for API key auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Api-Key", apiKey);
            
            // Create the request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("Text", zipCode);
            requestBody.put("MaxResults", 10);
            requestBody.put("FilterBBox", new double[0]);
            requestBody.put("Language", "en");
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Sending request to AWS Location Service with ZIP: {}, URL: {}", zipCode, url);
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
     * Parse the JSON response from AWS Location Service into our AddressResult model.
     * 
     * @param responseBody The JSON response body
     * @return AddressResult object
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
