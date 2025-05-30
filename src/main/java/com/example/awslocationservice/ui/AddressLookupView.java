package com.example.awslocationservice.ui;

import com.example.awslocationservice.model.AddressResult;
import com.example.awslocationservice.service.LocationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main UI view for the AWS Location Service Address Lookup application.
 * <p>
 * This Vaadin view provides an interface for users to enter ZIP codes,
 * validate them, and view the corresponding address information with a map.
 * </p>
 *
 * @author DelShahab
 * @version 1.0
 * @since 2025-05-30
 */
@Route("")
@PageTitle("AWS Location Service - Address Lookup")
public class AddressLookupView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AddressLookupView.class);

    private final transient LocationService locationService;
    
    private final TextField zipCodeField;
    private final Button searchButton;
    private final Div resultsDiv;

    /**
     * Constructor that sets up the UI components and layout.
     *
     * @param locationService The service for validating and looking up address information
     */
    public AddressLookupView(LocationService locationService) {
        this.locationService = locationService;

        // Set up the main layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Create page title
        H2 title = new H2("AWS Location Service - ZIP Code Lookup");
        
        // Create ZIP code input field
        zipCodeField = new TextField("Enter ZIP Code");
        zipCodeField.setPlaceholder("e.g. 92021");
        zipCodeField.setPattern("^\\d{5}(?:-\\d{4})?$"); 
        zipCodeField.setMinLength(5);
        zipCodeField.setMaxLength(10);
        zipCodeField.setRequiredIndicatorVisible(true);
        zipCodeField.setClearButtonVisible(true);
        zipCodeField.setWidthFull();
        
        // Create search button
        searchButton = new Button("Lookup Address");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> lookupAddress());
        // Add keyboard shortcut (ENTER key)
        searchButton.addClickShortcut(Key.ENTER);
        
        // Create horizontal layout for input and button
        HorizontalLayout inputLayout = new HorizontalLayout(zipCodeField, searchButton);
        inputLayout.setWidthFull();
        
        // Create results div
        resultsDiv = new Div();
        resultsDiv.addClassName("results-container");
        resultsDiv.setWidthFull();
        
        // Add components to the main layout
        add(title, inputLayout, resultsDiv);
    }
    
    /**
     * Perform address lookup with the entered ZIP code
     */
    private void lookupAddress() {
        String zipCode = zipCodeField.getValue().trim();
        
        if (zipCode.isEmpty()) {
            showNotification("Please enter a ZIP code", true);
            return;
        }
        
        if (!locationService.isValidZipCode(zipCode)) {
            showNotification("Invalid ZIP code format. Please enter a valid US ZIP code (e.g., 92021 or 92021-1234)", true);
            return;
        }
        
        log.debug("Looking up address for ZIP code: {}", zipCode);
        
        try {
            // Clear previous results
            resultsDiv.removeAll();
            
            // Perform lookup
            AddressResult addressResult = locationService.lookupAddressByZipCode(zipCode);
            
            // Display results
            displayResults(addressResult);
            
        } catch (Exception e) {
            log.error("Error during address lookup", e);
            showNotification("An unexpected error occurred: " + e.getMessage(), true);
        }
    }
    
    /**
     * Display the address results on the UI
     */
    private void displayResults(AddressResult addressResult) {
        if (addressResult == null) {
            showNotification("Error retrieving address information", true);
            return;
        }
        
        if (!"SUCCESS".equals(addressResult.getStatus())) {
            resultsDiv.add(createErrorMessage(addressResult.getErrorMessage()));
            return;
        }
        
        if (addressResult.getPlaces() == null || addressResult.getPlaces().isEmpty()) {
            resultsDiv.add(new Paragraph("No address information found for this ZIP code."));
            return;
        }
        
        // Add formatted address
        Div addressContainer = new Div();
        addressContainer.addClassName("address-container");
        
        H2 addressHeader = new H2("Address Information");
        addressContainer.add(addressHeader);
        
        Paragraph formattedAddressPara = new Paragraph("Full Address: " + addressResult.getFormattedAddress());
        addressContainer.add(formattedAddressPara);
        
        // Add location details
        AddressResult.Place place = addressResult.getPlaces().get(0);
        
        Div detailsDiv = new Div();
        detailsDiv.addClassName("address-details");
        
        if (place.getAddressNumber() != null && !place.getAddressNumber().isEmpty()) {
            detailsDiv.add(new Paragraph("Street Number: " + place.getAddressNumber()));
        }
        
        if (place.getStreet() != null && !place.getStreet().isEmpty()) {
            detailsDiv.add(new Paragraph("Street: " + place.getStreet()));
        }
        
        if (place.getMunicipality() != null && !place.getMunicipality().isEmpty()) {
            detailsDiv.add(new Paragraph("City: " + place.getMunicipality()));
        }
        
        if (place.getRegion() != null && !place.getRegion().isEmpty()) {
            detailsDiv.add(new Paragraph("State: " + place.getRegion()));
        }
        
        if (place.getPostalCode() != null && !place.getPostalCode().isEmpty()) {
            detailsDiv.add(new Paragraph("Postal Code: " + place.getPostalCode()));
        }
        
        if (place.getCountry() != null && !place.getCountry().isEmpty()) {
            detailsDiv.add(new Paragraph("Country: " + place.getCountry()));
        }
        
        addressContainer.add(detailsDiv);
        
        // Add coordinates
        Paragraph coordinatesPara = new Paragraph(
                "Coordinates: " + addressResult.getLatitude() + ", " + addressResult.getLongitude());
        addressContainer.add(coordinatesPara);
        
        resultsDiv.add(addressContainer);
    }
    

    
    /**
     * Create an error message component
     */
    private Component createErrorMessage(String message) {
        Div errorDiv = new Div();
        errorDiv.addClassName("error-message");
        
        Paragraph errorHeader = new Paragraph("Error");
        errorHeader.getStyle().set("font-weight", "bold");
        errorHeader.getStyle().set("color", "red");
        
        Paragraph errorContent = new Paragraph(message);
        
        errorDiv.add(errorHeader, errorContent);
        return errorDiv;
    }
    
    /**
     * Show a notification message
     */
    private void showNotification(String message, boolean isError) {
        Notification notification = Notification.show(message);
        notification.setDuration(3000);
        
        if (isError) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }
}
