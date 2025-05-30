# AWS Location Service - Address Validation

![AWS Location Service](https://d2908q01vomqb2.cloudfront.net/cb4e5208b4cd87268b208e49452ed6e89a68e0b8/2021/12/14/Amazon-Location-Service-architecture.png)

A comprehensive Spring Boot application with a Vaadin UI for validating US ZIP codes and retrieving address information using Amazon Location Service. This project demonstrates how to integrate Amazon Location Service with a modern Java web application.

## Table of Contents
- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [AWS Configuration](#aws-configuration)
  - [Setting up Place Index](#setting-up-place-index)
  - [Setting up Map Resource](#setting-up-map-resource)
- [Installation](#installation)
- [Configuration](#configuration)
- [Build and Run](#build-and-run)
- [Usage](#usage)
- [API Reference](#api-reference)
- [Implementation Details](#implementation-details)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

## Features

- Validate US ZIP code format using regex pattern matching
- Lookup address information using Amazon Location Service with HERE provider
- Display formatted address results with complete address components
- Interactive map preview using AWS Location Service Maps API
- Support for keyboard shortcut (ENTER key) to trigger validation
- Comprehensive error handling and logging
- Developer-friendly codebase with clean separation of concerns

## Architecture Overview

The application follows a standard Spring Boot architecture with layered components:

```
┌─────────────────────────┐
│   Vaadin UI Components  │
│  (AddressLookupView)    │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│   Spring Boot Services  │
│   (LocationService)     │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│  AWS Location Service   │
│     REST API            │
└─────────────────────────┘
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- AWS Account with Amazon Location Service configured
- Place Index named "ZipLookupIndex" in the us-west-2 region
- Map resource named "ExampleMap" (or any custom name)
- API Key for Amazon Location Service

## AWS Configuration

### Setting up Place Index

1. Sign in to the [AWS Management Console](https://console.aws.amazon.com/)
2. Navigate to Amazon Location Service
3. Select "Place indexes" from the sidebar
4. Click "Create place index"
5. Set the following properties:
   - Name: ZipLookupIndex
   - Data provider: HERE
   - Data storage option: The Places service doesn't store your results
6. Click "Create place index"

### Setting up Map Resource

1. In the Amazon Location Service dashboard, select "Maps" from the sidebar
2. Click "Create map resource"
3. Set the following properties:
   - Name: ExampleMap
   - Style: Any style of your choice
   - Map use case: Select appropriate options based on your needs
4. Click "Create map resource"

### Creating API Key

1. In the Amazon Location Service dashboard, select "API keys" from the sidebar
2. Click "Create API key"
3. Set a name and configure key restrictions as needed
4. Click "Create API key"
5. Copy the generated API key for use in the application

## Installation

```bash
# Clone the repository
git clone https://github.com/DelShahab/aws-location-service-demo.git
cd aws-location-service-demo

# Install dependencies
mvn install
```

## Configuration

Before running the application, update the `src/main/resources/application.properties` file with your AWS Location Service credentials:

```properties
# AWS Location Service configuration
aws.location.api-key=your-api-key-here
aws.location.place-index-name=ZipLookupIndex
aws.location.region=us-west-2
aws.location.map-name=ExampleMap
```

For different environments, you can use Spring profiles by creating profile-specific properties files:
- `application-dev.properties`
- `application-prod.properties`

## Build and Run

To build and run the application:

```bash
# Build the project
mvn clean package

# Run the application (dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Alternatively, run the JAR directly
java -jar -Dspring.profiles.active=dev target/aws-location-service-demo-0.0.1-SNAPSHOT.jar
```

The application will start on http://localhost:8080

## Usage

1. Enter a valid US ZIP code in the input field (e.g., "92021" or "92021-1234")
2. Click the "Lookup Address" button or press ENTER
3. The application will validate the ZIP code format
4. If valid, it will query Amazon Location Service for address information
5. The address information will be displayed along with a map showing the location

## API Reference

### LocationService

```java
// Validate a ZIP code
boolean isValidZipCode(String zipCode);

// Lookup address information by ZIP code
AddressResult lookupAddressByZipCode(String zipCode);
```

### AddressResult Model

```java
AddressResult result = AddressResult.builder()
    .formattedAddress("123 Main St, San Diego, CA 92021")
    .latitude(32.8328)
    .longitude(-116.9701)
    .places(listOfPlaces)
    .status("SUCCESS")
    .build();
```

## Implementation Details

- **LocationService**: Core service for validating ZIP codes and communicating with AWS Location Service using RestTemplate
- **AddressLookupView**: Vaadin UI component providing user interface with reactive components
- **AddressResult**: Data model with builder pattern for easy result construction
- **AWS Location Integration**: RESTful API integration with proper API key authentication
- **Map Rendering**: Client-side integration with AWS Location Service Maps using JavaScript libraries

## Screenshots

![ZIP Code Input](https://example.com/images/zipcode-input.png)
*The ZIP code input form with validation*

![Address Results](https://example.com/images/address-results.png)
*Address results with map display*

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
