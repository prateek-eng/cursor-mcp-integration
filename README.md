# Spring Boot PostgreSQL Demo Application

This is a Spring Boot application that demonstrates REST API interactions with a PostgreSQL database using JPA/Hibernate ORM.

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main Spring Boot application
├── controller/
│   └── PersonController.java     # REST API endpoints
├── entity/
│   └── Person.java              # JPA entity mapping
└── repository/
    └── PersonRepository.java    # Data access layer
```

## Features

- **CRUD Operations**: Create, Read, Update, Delete operations for Person entities
- **Custom Queries**: Advanced search and filtering capabilities
- **RESTful API**: Standard HTTP methods (GET, POST, PUT, DELETE)
- **JPA/Hibernate**: Object-relational mapping with PostgreSQL

## API Endpoints

### Basic CRUD Operations
- `GET /api/people` - Get all people
- `GET /api/people/{id}` - Get person by ID
- `POST /api/people` - Create new person
- `PUT /api/people/{id}` - Update person
- `DELETE /api/people/{id}` - Delete person

### Advanced Queries
- `GET /api/people/role/{role}` - Get people by role
- `GET /api/people/email/{email}` - Get person by email
- `GET /api/people/search?name={name}` - Search people by name
- `GET /api/people/count/role/{role}` - Count people by role

## Database Schema

The application creates a `people` table with the following structure:

```sql
CREATE TABLE people (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP
);
```

## Setup Instructions

1. **Install PostgreSQL** and create a database named `people_db`
2. **Update application.properties** with your database credentials
3. **Run the application**: `mvn spring-boot:run`
4. **Access the API** at `http://localhost:8080/api/people`

## Sample Data

You can test the API with sample data:

```json
{
  "name": "John Doe",
  "role": "Developer",
  "email": "john.doe@example.com"
}
```

## Migration Analysis

This project serves as a reference for analyzing:
- PostgreSQL schema structure
- JPA entity mappings
- REST API patterns
- Database migration strategies to MongoDB
