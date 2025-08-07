# Spring Boot MCP Integration with MongoDB Atlas

This is a Spring Boot application that demonstrates REST API interactions with both PostgreSQL and MongoDB databases, including MCP (Model Context Protocol) integration with MongoDB Atlas.

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main Spring Boot application
├── controller/
│   ├── PersonController.java     # REST API endpoints for PostgreSQL
│   ├── PersonMongoController.java # REST API endpoints for MongoDB
│   └── MigrationController.java  # Data migration endpoints
├── entity/
│   ├── Person.java              # JPA entity mapping for PostgreSQL
│   └── PersonMongo.java         # MongoDB document mapping
├── repository/
│   ├── PersonRepository.java    # PostgreSQL data access layer
│   └── PersonMongoRepository.java # MongoDB data access layer
└── service/
    └── MigrationService.java    # Data migration logic
```

## Features

- **CRUD Operations**: Create, Read, Update, Delete operations for Person entities
- **Dual Database Support**: PostgreSQL and MongoDB integration
- **Data Migration**: Tools to migrate data between PostgreSQL and MongoDB
- **MCP Integration**: Model Context Protocol integration with MongoDB Atlas
- **Custom Queries**: Advanced search and filtering capabilities
- **RESTful API**: Standard HTTP methods (GET, POST, PUT, DELETE)
- **JPA/Hibernate**: Object-relational mapping with PostgreSQL
- **MongoDB**: Document-based storage with MongoDB Atlas

## API Endpoints

### PostgreSQL Operations
- `GET /api/people` - Get all people from PostgreSQL
- `GET /api/people/{id}` - Get person by ID from PostgreSQL
- `POST /api/people` - Create new person in PostgreSQL
- `PUT /api/people/{id}` - Update person in PostgreSQL
- `DELETE /api/people/{id}` - Delete person from PostgreSQL

### MongoDB Operations
- `GET /api/mongo/people` - Get all people from MongoDB
- `GET /api/mongo/people/{id}` - Get person by ID from MongoDB
- `POST /api/mongo/people` - Create new person in MongoDB
- `PUT /api/mongo/people/{id}` - Update person in MongoDB
- `DELETE /api/mongo/people/{id}` - Delete person from MongoDB

### Migration Operations
- `POST /api/migration/start` - Start data migration from PostgreSQL to MongoDB
- `GET /api/migration/status` - Get migration status
- `POST /api/migration/verify` - Verify migration results

### Advanced Queries
- `GET /api/people/role/{role}` - Get people by role from PostgreSQL
- `GET /api/people/email/{email}` - Get person by email from PostgreSQL
- `GET /api/people/search?name={name}` - Search people by name in PostgreSQL
- `GET /api/people/count/role/{role}` - Count people by role in PostgreSQL

## Database Schema

### PostgreSQL Schema
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

### MongoDB Schema
The application uses a `people` collection with the following document structure:

```json
{
  "_id": "ObjectId",
  "name": "String",
  "role": "String", 
  "email": "String",
  "createdAt": "Date"
}
```

## Setup Instructions

1. **Install PostgreSQL** and create a database named `people_db`
2. **Set up MongoDB Atlas** cluster and get connection string
3. **Update application.properties** with your database credentials
4. **Run the application**: `mvn spring-boot:run`
5. **Access the API** at `http://localhost:8080/api/people`

## MCP Integration

This project demonstrates MCP (Model Context Protocol) integration with MongoDB Atlas, providing:
- Direct database operations through MCP tools
- Real-time data access and manipulation
- Seamless integration with AI assistants
- Enhanced development workflow

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
- MongoDB document structure
- JPA entity mappings
- REST API patterns
- Database migration strategies between PostgreSQL and MongoDB
- MCP integration patterns
