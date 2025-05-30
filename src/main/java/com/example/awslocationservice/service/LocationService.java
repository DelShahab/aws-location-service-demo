package com.example.awslocationservice.service;

import com.amazonaws.services.location.AmazonLocation;
import com.amazonaws.services.location.model.Place;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextRequest;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextResult;
import com.amazonaws.services.location.model.SearchForTextResult;
import com.example.awslocationservice.config.AWSLocationProperties;
import com.example.awslocationservice.model.AddressResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for interacting with AWS Location Service to validate ZIP codes and retrieve address information.
 * <p>
 * This service provides methods to validate US ZIP code formats and to look up addresses using
 * the AWS Location Service SDK with the HERE provider. It implements AWS credentials authentication
 * (access key and secret key) and handles all necessary communication and response parsing.
 * </p>
 * 
 * @author DelShahab
 * @version 1.1
 * @since 2025-05-30
 */
@Service
@Slf4j
public class LocationService {

    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:-\\d{4})?$");

    private final AWSLocationProperties awsLocationProperties;
    private final AmazonLocation amazonLocationClient;

    /**
     * Constructor that injects the required dependencies.
     * 
     * @param awsLocationProperties Configuration properties for AWS Location Service API
     * @param amazonLocationClient AmazonLocation client for making requests to AWS Location Service
     */
    public LocationService(AWSLocationProperties awsLocationProperties, AmazonLocation amazonLocationClient) {
        this.awsLocationProperties = awsLocationProperties;
        this.amazonLocationClient = amazonLocationClient;
    }

    /**
     * Validates if a string is a valid US ZIP code format.
     * 
     * @param zipCode The ZIP code string to validate
     * @return true if the ZIP code format is valid, false otherwise
     */
    public boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.isEmpty()) {
            return false;
        }
        
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Looks up address information for a given ZIP code using AWS Location Service.
     *
     * @param zipCode The ZIP code to look up (e.g., "92021" or "92021-1234")
     * @return AddressResult containing the address details or error information
     */
    public AddressResult lookupAddressByZipCode(String zipCode) {
        log.info("Looking up address for ZIP code: {}", zipCode);
        
        // Validate input
        if (zipCode == null || zipCode.trim().isEmpty()) {
            log.error("ZIP code is null or empty");
            return AddressResult.builder()
                .status(STATUS_ERROR)
                .errorMessage("ZIP code cannot be null or empty")
                .build();
        }
        
        // Validate ZIP code format
        if (!isValidZipCode(zipCode)) {
            log.error("Invalid ZIP code format: {}", zipCode);
            return AddressResult.builder()
                .status(STATUS_ERROR)
                .errorMessage("Invalid ZIP code format")
                .build();
        }

        try {
            // Create request for AWS Location Service SDK
            SearchPlaceIndexForTextRequest request = new SearchPlaceIndexForTextRequest()
                .withIndexName(awsLocationProperties.getPlaceIndexName())
                .withText(zipCode)
                .withMaxResults(5);
            
            log.debug("Sending request to AWS Location Service: {}", request);
            
            // Make the API call using AWS SDK
            SearchPlaceIndexForTextResult result = amazonLocationClient.searchPlaceIndexForText(request);
            
            log.debug("Received successful response from AWS Location Service");
            return parseAwsLocationServiceResponse(result, zipCode);
            
        } catch (Exception e) {
            log.error("Error calling AWS Location Service", e);
            return AddressResult.builder()
                .status(STATUS_ERROR)
                .errorMessage("Error calling AWS Location Service: " + e.getMessage())
                .build();
        }
    }

    /**
     * Parses the AWS Location Service SDK response.
     *
     * @param result The response from AWS Location Service SDK
     * @param zipCode The original ZIP code used in the request
     * @return AddressResult with parsed address information
     */
    private AddressResult parseAwsLocationServiceResponse(SearchPlaceIndexForTextResult result, String zipCode) {
        try {
            if (result.getResults().isEmpty()) {
                log.warn("No results found for ZIP code: {}", zipCode);
                return AddressResult.builder()
                    .status(STATUS_SUCCESS)
                    .zipCode(zipCode)
                    .build();
            }
            
            // Process the first result (most relevant)
            SearchForTextResult firstResult = result.getResults().get(0);
            Place place = firstResult.getPlace();
            
            // Extract address components
            String label = place.getLabel() != null ? place.getLabel() : "";
            String addressNumber = place.getAddressNumber() != null ? place.getAddressNumber() : "";
            String street = place.getStreet() != null ? place.getStreet() : "";
            String municipality = place.getMunicipality() != null ? place.getMunicipality() : "";
            String region = place.getRegion() != null ? place.getRegion() : "";
            String subRegion = place.getSubRegion() != null ? place.getSubRegion() : "";
            String postalCode = place.getPostalCode() != null ? place.getPostalCode() : zipCode; // Default to input if not present
            String country = place.getCountry() != null ? place.getCountry() : "";
            
            // Get coordinates if available
            double latitude = 0.0;
            double longitude = 0.0;
            
            if (result.getSummary() != null && result.getSummary().getResultBBox() != null && result.getSummary().getResultBBox().size() >= 4) {
                // Calculate center point from bounding box
                double west = result.getSummary().getResultBBox().get(0);
                double south = result.getSummary().getResultBBox().get(1);
                double east = result.getSummary().getResultBBox().get(2);
                double north = result.getSummary().getResultBBox().get(3);
                
                longitude = (west + east) / 2;
                latitude = (south + north) / 2;
            }
            
            // Build list of places
            List<AddressResult.Place> places = new ArrayList<>();
            
            AddressResult.Place primaryPlace = AddressResult.Place.builder()
                .addressNumber(addressNumber)
                .street(street)
                .municipality(municipality)
                .region(region)
                .subRegion(subRegion)
                .postalCode(postalCode)
                .country(country)
                .build();
            
            places.add(primaryPlace);
            
            // Add additional results if available
            for (int i = 1; i < Math.min(result.getResults().size(), 5); i++) {
                SearchForTextResult additionalResult = result.getResults().get(i);
                Place additionalPlace = additionalResult.getPlace();
                
                AddressResult.Place place2 = AddressResult.Place.builder()
                    .addressNumber(additionalPlace.getAddressNumber() != null ? additionalPlace.getAddressNumber() : "")
                    .street(additionalPlace.getStreet() != null ? additionalPlace.getStreet() : "")
                    .municipality(additionalPlace.getMunicipality() != null ? additionalPlace.getMunicipality() : "")
                    .region(additionalPlace.getRegion() != null ? additionalPlace.getRegion() : "")
                    .subRegion(additionalPlace.getSubRegion() != null ? additionalPlace.getSubRegion() : "")
                    .postalCode(additionalPlace.getPostalCode() != null ? additionalPlace.getPostalCode() : "")
                    .country(additionalPlace.getCountry() != null ? additionalPlace.getCountry() : "")
                    .build();
                
                places.add(place2);
            }
            
            // Build and return the result
            return AddressResult.builder()
                .status(STATUS_SUCCESS)
                .zipCode(zipCode)
                .formattedAddress(label)
                .latitude(latitude)
                .longitude(longitude)
                .places(places)
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
