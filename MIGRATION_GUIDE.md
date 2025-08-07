# PostgreSQL to MongoDB Migration Guide

## Overview

This guide provides a complete step-by-step process for migrating from PostgreSQL to MongoDB using the Spring Boot application we've created. The migration includes both data migration and API layer changes.

## Prerequisites

1. **PostgreSQL Database**: Running with sample data
2. **MongoDB Database**: Running and accessible
3. **Spring Boot Application**: Built and ready to run
4. **MCP Servers**: Configured for both PostgreSQL and MongoDB

## Step 1: Schema Analysis and Comparison

### PostgreSQL Schema
```sql
CREATE TABLE people (
    id BIGSERIAL PRIMARY KEY,           -- Auto-incrementing integer
    name VARCHAR(255) NOT NULL,         -- Person's name (required)
    role VARCHAR(255) NOT NULL,         -- Person's role (required)
    email VARCHAR(255),                 -- Person's email (optional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- Creation timestamp
);
```

### MongoDB Schema
```javascript
{
  "_id": ObjectId("..."),              // Auto-generated ObjectId
  "name": "John Doe",                   // String (required)
  "role": "Developer",                  // String (required)
  "email": "john.doe@example.com",     // String (optional)
  "createdAt": "2025-08-07T08:09:25.368Z",  // ISO date string
  "postgresId": 1                      // Legacy PostgreSQL ID for migration
}
```

## Step 2: Data Migration Process

### 2.1 Verify Current Data

First, check the current data in PostgreSQL:

```bash
# Using MCP PostgreSQL tools
curl -X GET "http://localhost:8080/api/people"
```

Expected response:
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "role": "Developer",
    "email": "john.doe@example.com",
    "created_at": "2025-08-07T08:09:25.368Z"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "role": "Manager",
    "email": "jane.smith@example.com",
    "created_at": "2025-08-07T08:09:27.122Z"
  }
]
```

### 2.2 Start Migration

Use the migration API to migrate all data:

```bash
# Migrate all data from PostgreSQL to MongoDB
curl -X POST "http://localhost:8080/api/migration/migrate-all"
```

Expected response:
```json
{
  "success": true,
  "totalRecords": 3,
  "migrated": 3,
  "skipped": 0,
  "failed": 0,
  "errors": []
}
```

### 2.3 Verify Migration

Check migration status:

```bash
# Verify migration
curl -X GET "http://localhost:8080/api/migration/verify"
```

Expected response:
```json
{
  "success": true,
  "postgresCount": 3,
  "mongoCount": 3,
  "migratedCount": 3,
  "countsMatch": true,
  "verified": 3,
  "mismatched": 0,
  "missing": 0,
  "errors": []
}
```

### 2.4 Check Migration Status

Get detailed migration status:

```bash
# Get migration status
curl -X GET "http://localhost:8080/api/migration/status"
```

Expected response:
```json
{
  "postgresCount": 3,
  "mongoCount": 3,
  "migratedCount": 3,
  "countsMatch": true,
  "migrationComplete": true,
  "migrationProgress": 100.0
}
```

## Step 3: API Layer Testing

### 3.1 Test PostgreSQL API (Original)

```bash
# Get all people from PostgreSQL
curl -X GET "http://localhost:8080/api/people"

# Get person by ID (integer)
curl -X GET "http://localhost:8080/api/people/1"

# Search by role
curl -X GET "http://localhost:8080/api/people/role/Developer"
```

### 3.2 Test MongoDB API (New)

```bash
# Get all people from MongoDB
curl -X GET "http://localhost:8080/api/mongo/people"

# Get person by ObjectId
curl -X GET "http://localhost:8080/api/mongo/people/68945f5878997b510b64974a"

# Get person by legacy PostgreSQL ID (backward compatibility)
curl -X GET "http://localhost:8080/api/mongo/people/1"

# Search by role
curl -X GET "http://localhost:8080/api/mongo/people/role/Developer"
```

### 3.3 Compare Response Formats

**PostgreSQL Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "role": "Developer",
  "email": "john.doe@example.com",
  "created_at": "2025-08-07T08:09:25.368Z"
}
```

**MongoDB Response:**
```json
{
  "_id": "68945f5878997b510b64974a",
  "name": "John Doe",
  "role": "Developer",
  "email": "john.doe@example.com",
  "createdAt": "2025-08-07T08:09:25.368Z",
  "postgresId": 1
}
```

## Step 4: Advanced Features Testing

### 4.1 Migration-Specific Endpoints

```bash
# Get all people with PostgreSQL IDs
curl -X GET "http://localhost:8080/api/mongo/people/migration/postgres-ids"

# Get person by PostgreSQL ID
curl -X GET "http://localhost:8080/api/mongo/people/migration/postgres-id/1"
```

### 4.2 Advanced Queries

```bash
# Search by name and role
curl -X GET "http://localhost:8080/api/mongo/people/advanced/search?name=John&role=Developer"

# Get people by role created after date
curl -X GET "http://localhost:8080/api/mongo/people/advanced/role/Developer/after?startDate=2025-08-07T00:00:00"

# Get all unique roles
curl -X GET "http://localhost:8080/api/mongo/people/roles"
```

## Step 5: Rollback Process

If migration needs to be rolled back:

```bash
# Rollback migration (removes migrated records from MongoDB)
curl -X POST "http://localhost:8080/api/migration/rollback"
```

Expected response:
```json
{
  "success": true,
  "totalRecords": 3,
  "migrated": 3,
  "skipped": 0,
  "failed": 0,
  "errors": []
}
```

## Step 6: Production Migration Strategy

### 6.1 Blue-Green Deployment

1. **Blue Environment**: Running with PostgreSQL
2. **Green Environment**: Running with MongoDB
3. **Data Migration**: Migrate data from Blue to Green
4. **Testing**: Verify Green environment functionality
5. **Switch**: Route traffic from Blue to Green
6. **Cleanup**: Decommission Blue environment

### 6.2 Gradual Migration

1. **Phase 1**: Deploy MongoDB alongside PostgreSQL
2. **Phase 2**: Migrate read operations to MongoDB
3. **Phase 3**: Migrate write operations to MongoDB
4. **Phase 4**: Remove PostgreSQL dependencies

### 6.3 API Versioning

```bash
# Version 1 (PostgreSQL)
GET /api/v1/people/{id}

# Version 2 (MongoDB)
GET /api/v2/people/{id}
```

## Step 7: Monitoring and Validation

### 7.1 Data Consistency Checks

```bash
# Verify data integrity
curl -X GET "http://localhost:8080/api/migration/verify"

# Check migration progress
curl -X GET "http://localhost:8080/api/migration/status"
```

### 7.2 Performance Monitoring

- Monitor query performance in MongoDB
- Compare response times between PostgreSQL and MongoDB
- Check index usage and optimization

### 7.3 Error Handling

- Monitor migration errors
- Check for data inconsistencies
- Validate business logic integrity

## Step 8: Post-Migration Tasks

### 8.1 Cleanup

1. Remove PostgreSQL dependencies from application
2. Update configuration files
3. Remove migration-related code
4. Update documentation

### 8.2 Optimization

1. Create MongoDB indexes for performance
2. Optimize queries for MongoDB
3. Implement MongoDB-specific features
4. Update caching strategies

### 8.3 Documentation

1. Update API documentation
2. Update deployment guides
3. Update monitoring dashboards
4. Update disaster recovery procedures

## Troubleshooting

### Common Issues

1. **Connection Issues**: Check MongoDB connection settings
2. **Data Type Mismatches**: Verify field mappings
3. **Index Creation Failures**: Check MongoDB permissions
4. **Validation Errors**: Review data constraints

### Debug Commands

```bash
# Check MongoDB connection
curl -X GET "http://localhost:8080/api/mongo/people"

# Check PostgreSQL connection
curl -X GET "http://localhost:8080/api/people"

# Check migration status
curl -X GET "http://localhost:8080/api/migration/status"
```

## Conclusion

This migration guide provides a comprehensive approach to moving from PostgreSQL to MongoDB while maintaining data integrity and API compatibility. The step-by-step process ensures a smooth transition with minimal downtime and risk.

The key benefits of this migration include:
- **Flexible Schema**: Easier to evolve data model
- **Scalability**: Better horizontal scaling capabilities
- **Performance**: Optimized for read-heavy workloads
- **Developer Experience**: More intuitive document-based queries
