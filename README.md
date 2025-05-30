# AWS Location Service - Address Validation

![AWS Location Service](https://d2908q01vomqb2.cloudfront.net/cb4e5208b4cd87268b208e49452ed6e89a68e0b8/2021/12/14/Amazon-Location-Service-architecture.png)

A comprehensive Spring Boot application with a Vaadin UI for validating US ZIP codes and retrieving address information using Amazon Location Service. This project demonstrates how to integrate Amazon Location Service with AWS SDK and credentials authentication in a modern Java web application.

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
- Lookup address information using AWS Location Service SDK with HERE provider
- Display formatted address results with complete address components
- Support for keyboard shortcut (ENTER key) to trigger validation
- Hybrid authentication using both AWS credentials and API key for enhanced security
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
│     AWS SDK Client      │
│   (AmazonLocation)      │
└───────────┬─────────────┘
            ↓
┌─────────────────────────┐
│  AWS Location Service   │
└─────────────────────────┘
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- AWS Account with Amazon Location Service configured
- Place Index named "ZipLookupIndex" in the us-west-2 region
- AWS access key and secret key with permissions for AWS Location Service
- Optional API key for additional request authentication

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

### Setting up AWS Credentials

1. In the AWS Management Console, navigate to IAM (Identity and Access Management)
2. Create a user or use an existing user with programmatic access
3. Attach the `AmazonLocationFullAccess` policy to the user
4. Generate or retrieve the access key and secret key
5. Copy these credentials for use in the application configuration

## Installation

```bash
# Clone the repository
git clone https://github.com/DelShahab/aws-location-service-demo.git
cd aws-location-service-demo

# Install dependencies
mvn install
```

## Configuration

To configure the application, update the properties files with your AWS credentials and Location Service settings:

### For Development

Edit `src/main/resources/application-dev.properties`:

```properties
# AWS credentials for development environment
aws.credentials.access-key=your-access-key-here
aws.credentials.secret-key=your-secret-key-here

# AWS Location Service configuration
aws.location.place-index-name=ZipLookupIndex
aws.location.region=us-west-2
aws.location.data-provider=Here
aws.location.api-key=your-api-key-here  # Optional for enhanced security
```

### For Production

Edit `src/main/resources/application-prod.properties`:

```properties
# AWS credentials for production environment
aws.credentials.access-key=${AWS_ACCESS_KEY_ID}
aws.credentials.secret-key=${AWS_SECRET_ACCESS_KEY}

# AWS Location Service configuration
aws.location.place-index-name=${AWS_LOCATION_PLACE_INDEX_NAME:ZipLookupIndex}
aws.location.region=${AWS_LOCATION_REGION:us-west-2}
aws.location.data-provider=${AWS_LOCATION_DATA_PROVIDER:Here}
aws.location.api-key=${AWS_LOCATION_API_KEY}  # Optional for enhanced security
```

For production, use environment variables to set sensitive information like AWS credentials.

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

1. Enter a ZIP code in the input field
2. Click the "Validate" button or press ENTER
3. The application will first validate the ZIP code format
4. If valid, it will query Amazon Location Service for address information
5. The address information will be displayed with all available address components

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
