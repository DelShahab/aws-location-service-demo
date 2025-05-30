package com.example.awslocationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the result of an address lookup operation from AWS Location Service.
 * <p>
 * This class encapsulates all the data returned from an address lookup operation,
 * including formatted address text, geographic coordinates, and status information.
 * It uses the Builder pattern for easy construction and Lombok annotations for
 * reducing boilerplate code.
 * </p>
 *
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResult {
    /** The full formatted address as a single string */
    private String formattedAddress;
    
    /** The ZIP code used for the lookup */
    private String zipCode;
    
    /** List of place details returned from the lookup */
    private List<Place> places;
    
    /** Latitude coordinate of the address */
    private double latitude;
    
    /** Longitude coordinate of the address */
    private double longitude;
    
    /** Status of the operation (SUCCESS, ERROR, NOT_FOUND) */
    private String status;
    
    /** Error message if the operation failed */
    private String errorMessage;

    /**
     * Represents a single place or address component in the address lookup result.
     * <p>
     * This inner class contains the detailed components of an address, such as
     * street name, municipality (city), region (state), and postal code.
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Place {
        /** Street number component of the address */
        private String addressNumber;
        
        /** Street name component of the address */
        private String street;
        
        /** City or municipality component of the address */
        private String municipality;
        
        /** Postal code (ZIP code) of the address */
        private String postalCode;
        
        /** State or region component of the address */
        private String region;
        
        /** County or sub-region component of the address */
        private String subRegion;
        
        /** Country component of the address */
        private String country;
        
        /** Human-readable label for the address */
        private String label;
    }
}
