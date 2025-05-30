package com.example.awslocationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResult {
    private String formattedAddress;
    private List<Place> places;
    private double latitude;
    private double longitude;
    private String status;
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Place {
        private String addressNumber;
        private String street;
        private String municipality;
        private String postalCode;
        private String region;
        private String country;
        private String label;
    }
}
