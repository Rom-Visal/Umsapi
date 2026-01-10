# Spring Boot API Project ![spring boot icon](https://img.icons8.com/color/48/spring-logo.png)

A RESTful web service built with Spring Boot, Focusing on clean code practices, Database optimization, and security design.

## Features

* **Data Mapping:** Integrated **MapStruct** to handle DTO-to-Entity conversions, reducing boilerplate and improving maintainability.
* **Database Optimization:** Resolved **N+1 select problems** using targeted queries and implemented **Database Indexing** to improve search performance.
* **Security:** Configured **Spring Security** with based authentication and custom entry points.
* **Advanced Relationships:** Implemented a Many-to-Many relationship between Users and Roles using a dedicated junction table with **Auditing** support.
  
  <img width="50%" alt="image" src="https://github.com/user-attachments/assets/80d5419d-4ba7-4397-81af-aec2b0caa123" />

* **Global Exception Handling:** Centralized error management using `@ControllerAdvice` to provide consistent API responses.
  
  <img width="70%" alt="image" src="https://github.com/user-attachments/assets/3ac9e564-8334-41e1-b075-48e013ba47ac" />

* **API Documentation:** Fully documented with **Swagger/OpenAPI**, including detailed request/response schemas.

## Tech Stack

* Java: 17+
* **Spring Boot**: 3.x
* **Data Access**: Spring Data JPA
* **Database**: PostgreSQL
* **Mapping**: MapStruct
* **API Docs**: SpringDoc OpenAPI

## Prerequisites
Before running this application, make sure you have

* Java 17 or higher
* PostgreSQL Database
* IDE (IntelliJ IDEA, Eclipse, VS Code)

## How to Run

1. **Clone the repository:**
   ```
    git clone https://github.com/Rom-Visal/Spring-Boot-REST-API.git
   
   ```
2. **Configure Database:**
   ```
   spring.datasource.url=jdbc:postgresql://localhost:3306/your_database
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
   ```
   
## API Access
Once the application is running, you can access the interactive API documentation at:

`http://localhost:8080/swagger-ui.html`
#### API Documentation Preview

Admin Endpoints
<img width="2784" height="807" alt="image" src="https://github.com/user-attachments/assets/e58314a2-3bd8-47d9-8a09-b617fb7ef71a" />

Other Endpoints
<img width="2802" height="1180" alt="image" src="https://github.com/user-attachments/assets/97fa68a2-7079-4500-aa6d-6597c440526c" />
