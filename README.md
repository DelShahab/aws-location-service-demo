# AWS Location Service - Address Validation

A Spring Boot application with a Vaadin UI for validating US ZIP codes and retrieving address information using Amazon Location Service.

## Features

- Validate US ZIP code format
- Lookup address information using Amazon Location Service with HERE provider
- Display formatted address results
- Show map preview using coordinates
- Support for keyboard shortcut (ENTER key) to trigger validation
- Error handling and logging

## Prerequisites

- Java 11 or higher
- Maven
- AWS Account with Amazon Location Service configured
- Place Index named "ZipLookupIndex" in the us-west-2 region
- API Key for Amazon Location Service

## Configuration

Before running the application, update the `src/main/resources/application.properties` file with your AWS Location Service API key:

```
aws.location.api-key=your-api-key-here
aws.location.place-index-name=ZipLookupIndex
aws.location.region=us-west-2
```

## Build and Run

To build and run the application:

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on http://localhost:8080

## Usage

1. Enter a valid US ZIP code in the input field (e.g., 92021)
2. Click the "Lookup Address" button or press ENTER
3. View the address information and map display if available

## Implementation Details

- **LocationService**: Core service for validating ZIP codes and communicating with AWS Location Service
- **AddressLookupView**: Vaadin UI component for the user interface
- **AddressResult**: Model for address data

## Note on Map Display

The current implementation uses a Google Maps static image URL for displaying maps. To display actual maps:
1. Replace the URL template in `AddressLookupView.java` with your mapping provider
2. Add your map API key if required
