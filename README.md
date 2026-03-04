# MedicalStore

## Overview
MedicalStore is a Spring Boot based application that provides a simple inventory management system for a medical store. It includes features for managing medicines, suppliers, and generating reports.

## Prerequisites
- Java 17
- Maven 3.8+
- MySQL database (or H2 for testing)

## Setup
1. Clone the repository:
   ```
   git clone <repo-url>
   ```
2. Navigate to the project directory:
   ```
   cd medicalstore
   ```
3. Configure the database connection in `src/main/resources/application.properties`.
4. Build the project:
   ```
   mvn clean install
   ```
5. Run the application:
   ```
   mvn spring-boot:run
   ```

## Building & Testing
- Compile: `mvn compile`
- Run tests: `mvn test`
- Package: `mvn package`

## Contributing
Feel free to open issues and submit pull requests. Follow the existing code style and include unit tests for new features.

## License
This project is licensed under the MIT License.
