package com.example.awslocationservice.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.awslocationservice.model.AddressResult;
import com.example.awslocationservice.service.LocationService;

import lombok.extern.slf4j.Slf4j;

/**
 * Test controller for the AWS Location Service integration.
 * <p>
 * This controller provides simple REST endpoints to test the Location Service
 * functionality directly via Postman or other API tools.
 * </p>
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
public class LocationTestController {

    private final LocationService locationService;

    /**
     * Constructor injection of dependencies.
     *
     * @param locationService The service for address lookups
     */
    public LocationTestController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Test endpoint to lookup an address by ZIP code.
     *
     * @param zipCode The ZIP code to lookup
     * @return AddressResult containing the address details or error information
     */
    @GetMapping("/lookup/{zipCode}")
    public ResponseEntity<AddressResult> testLookupZipCode(@PathVariable String zipCode) {
        log.info("Test endpoint called for ZIP code: {}", zipCode);
        AddressResult result = locationService.lookupAddressByZipCode(zipCode);
        return ResponseEntity.ok(result);
    }
}
