# Poker Application

This is a Poker application built using Java, Spring Boot, and PostgreSQL. The application allows users to create accounts, add friends, and participate in poker games.

## Table of Contents

- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Endpoints](#endpoints)
- [License](#license)

## Technologies Used

- Java
- Spring Boot
- PostgreSQL
- JPA/Hibernate
- Lombok
- Gradle
- JUnit
- Mockito

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL
- Gradle

### Installation

1. Clone the repository:
    ```sh
    git clone https://gitlab.com/kdg-ti/integratieproject-2/2024-2025/team5/spring-backend.git
    cd poker-application
    ```

2. Set up the PostgreSQL database:
    ```sql
    CREATE DATABASE poker_db;
    ```

3. Update the `application.properties` file with your database credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/poker_db
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```

4. Build the project:
    ```sh
    ./gradlew build
    ```
   
5. run the tests:
   ```sh
   ./gradlew test
   ```

## Configuration

### Security

The application uses JWT and keycloak for authentication and authorization. Update the `SecurityConfig` class to configure security settings.

### CORS

CORS is configured to allow requests from `http://localhost:3000` and `http://www.team5.eliasdh.com`. Update the `corsConfigurationSource` method in `SecurityConfig` if needed.

## Running the Application

To run the application, use the following command:
```sh
./gradlew bootRun
```

#### The application will be available at http://localhost:8080.