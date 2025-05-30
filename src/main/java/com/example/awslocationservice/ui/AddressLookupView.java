package com.example.awslocationservice.ui;

import com.example.awslocationservice.config.AWSLocationProperties;
import com.example.awslocationservice.model.AddressResult;
import com.example.awslocationservice.service.LocationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
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
    
    private final transient AWSLocationProperties awsLocationProperties;

    private final transient LocationService locationService;
    
    private final TextField zipCodeField;
    private final Button searchButton;
    private final Div resultsDiv;
    private final Div mapDiv;

    /**
     * Constructor that sets up the UI components and layout.
     *
     * @param locationService The service for validating and looking up address information
     * @param awsLocationProperties Configuration properties for AWS Location Service
     */
    public AddressLookupView(LocationService locationService, AWSLocationProperties awsLocationProperties) {
        this.locationService = locationService;
        this.awsLocationProperties = awsLocationProperties;

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
        
        // Create map div
        mapDiv = new Div();
        mapDiv.addClassName("map-container");
        mapDiv.setWidthFull();
        mapDiv.setVisible(false);
        
        // Add components to the main layout
        add(title, inputLayout, resultsDiv, mapDiv);
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
            mapDiv.removeAll();
            mapDiv.setVisible(false);
            
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
        
        // Add map preview
        displayMap(addressResult.getLatitude(), addressResult.getLongitude());
    }
    
    /**
     * Display a map with the provided coordinates using AWS Location Service
     */
    private void displayMap(double latitude, double longitude) {
        if (latitude == 0 && longitude == 0) {
            return;
        }
        
        try {
            H2 mapTitle = new H2("Location Map");
            
            // Create an HTML wrapper for AWS Location map
            String mapHtml = createAwsLocationMapHtml(latitude, longitude);
            
            // Create an IFrame to display the map
            IFrame mapFrame = new IFrame();
            mapFrame.getElement().setAttribute("srcdoc", mapHtml);
            mapFrame.setWidth("100%");
            mapFrame.setHeight("400px");
            mapFrame.getElement().getStyle().set("border", "none");
            
            Paragraph note = new Paragraph("Map powered by AWS Location Service");
            
            mapDiv.removeAll();
            mapDiv.add(mapTitle, mapFrame, note);
            mapDiv.setVisible(true);
            
        } catch (Exception e) {
            log.error("Failed to load map", e);
            showNotification("Failed to load map: " + e.getMessage(), true);
        }
    }
    
    /**
     * Creates HTML content with AWS Location Service map integration
     */
    private String createAwsLocationMapHtml(double latitude, double longitude) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>AWS Location Service Map</title>\n" +
                "    <script src=\"https://unpkg.com/amazon-location-client-js@1.x/dist/amazonLocationClient.js\"></script>\n" +
                "    <script src=\"https://unpkg.com/maplibre-gl@2.x/dist/maplibre-gl.js\"></script>\n" +
                "    <link href=\"https://unpkg.com/maplibre-gl@2.x/dist/maplibre-gl.css\" rel=\"stylesheet\">\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; }\n" +
                "        #map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        // Initialize the Amazon Location SDK client\n" +
                "        const client = new amazonLocationClient.LocationClient({\n" +
                "            credentials: { apiKey: '" + awsLocationProperties.getApiKey() + "' },\n" +
                "            region: '" + awsLocationProperties.getRegion() + "'\n" +
                "        });\n" +
                "\n" +
                "        // Initialize the map\n" +
                "        const map = new maplibregl.Map({\n" +
                "            container: 'map',\n" +
                "            style: client.getMapStyleDescriptor({\n" +
                "                mapName: '" + awsLocationProperties.getMapName() + "'\n" +
                "            }),\n" +
                "            center: [" + longitude + ", " + latitude + "],\n" +
                "            zoom: 15\n" +
                "        });\n" +
                "\n" +
                "        // Add a marker at the specified location\n" +
                "        map.on('load', () => {\n" +
                "            new maplibregl.Marker()\n" +
                "                .setLngLat([" + longitude + ", " + latitude + "])\n" +
                "                .addTo(map);\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
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
