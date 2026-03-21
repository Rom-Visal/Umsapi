# Role-Based Access Control API ![spring boot icon](https://img.icons8.com/color/48/spring-logo.png)

A comprehensive RESTful web service built with **Spring Boot 3.5**, focusing on **Role-Based Access Control (RBAC)**, secure authentication, and database optimization.

## 🚀 Key Features

* **Authentication & Authorization:** Secure JWT-based authentication with Access and Refresh tokens.
* **Role-Based Access Control (RBAC):** Managed user roles and permissions for secure endpoint access.
* **Data Mapping:** Integrated **MapStruct** for efficient DTO-to-Entity conversions, reducing boilerplate.
* **Database Optimization:** Resolved **N+1 select problems** using targeted queries and implemented **Database Indexing**.
* **Advanced Relationships:** Many-to-Many relationship between Users and Roles with **Auditing** support.
* **Global Exception Handling:** Centralized error management using `@ControllerAdvice` for consistent responses.
* **API Documentation:** Fully documented with **Swagger/OpenAPI 3**, including detailed schemas.

## 🛠️ Tech Stack

* **Java**: 21
* **Framework**: Spring Boot 3.5.11
* **Security**: Spring Security & JWT (jjwt)
* **Data Access**: Spring Data JPA
* **Database**: PostgreSQL
* **Mapping**: MapStruct
* **API Documentation**: SpringDoc OpenAPI 2.8.15
* **Utility**: Lombok

## 📋 Prerequisites

Before running this application, ensure you have:

* **Java 21** or higher
* **PostgreSQL** Database
* **Maven** (included via `./mvnw`)
* IDE (IntelliJ IDEA, Eclipse, or VS Code)

## ⚙️ Configuration

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Rom-Visal/Spring-Boot-REST-API.git
   cd Spring-Boot-REST-API
   ```

2. **Configure Database (src/main/resources/application.properties):**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Run the Application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 📖 API Documentation

Once the application is running, access the interactive Swagger UI at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

#### API Preview
![img.png](Documentation.png)

---
Developed by [Rom Visal](https://github.com/Rom-Visal)