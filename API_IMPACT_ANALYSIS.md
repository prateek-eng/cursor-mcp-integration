# API Layer Impact Analysis: PostgreSQL to MongoDB Migration

## Current API Structure Analysis

### Current Endpoints (PostgreSQL-based)

#### 1. Basic CRUD Operations
```java
// GET all people
@GetMapping
public ResponseEntity<List<Person>> getAllPeople()

// GET person by ID (uses Long id)
@GetMapping("/{id}")
public ResponseEntity<Person> getPersonById(@PathVariable Long id)

// POST create new person
@PostMapping
public ResponseEntity<Person> createPerson(@RequestBody Person person)

// PUT update person (uses Long id)
@PutMapping("/{id}")
public ResponseEntity<Person> updatePerson(@PathVariable Long id, @RequestBody Person personDetails)

// DELETE person (uses Long id)
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePerson(@PathVariable Long id)
```

#### 2. Advanced Query Operations
```java
// GET people by role
@GetMapping("/role/{role}")
public ResponseEntity<List<Person>> getPeopleByRole(@PathVariable String role)

// GET person by email
@GetMapping("/email/{email}")
public ResponseEntity<Person> getPersonByEmail(@PathVariable String email)

// GET people by name search
@GetMapping("/search")
public ResponseEntity<List<Person>> searchPeopleByName(@RequestParam String name)

// GET count by role
@GetMapping("/count/role/{role}")
public ResponseEntity<Long> getCountByRole(@PathVariable String role)
```

## MongoDB Migration Impact Analysis

### 1. ID Type Changes (HIGH IMPACT)

#### Current PostgreSQL Implementation:
```java
// Uses Long (integer) IDs
@GetMapping("/{id}")
public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
    Optional<Person> person = personRepository.findById(id);
    // ...
}
```

#### Required MongoDB Changes:
```java
// Must change to String (ObjectId)
@GetMapping("/{id}")
public ResponseEntity<Person> getPersonById(@PathVariable String id) {
    Optional<Person> person = personRepository.findById(id);
    // ...
}
```

**Impact Assessment:**
- **Breaking Change**: All client applications must handle string IDs instead of integers
- **API Compatibility**: Requires versioning or backward compatibility layer
- **Database Queries**: All ID-based queries need ObjectId conversion

### 2. Entity Class Changes (HIGH IMPACT)

#### Current PostgreSQL Entity:
```java
@Entity
@Table(name = "people")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Integer ID
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### Required MongoDB Entity:
```java
@Document(collection = "people")
public class Person {
    @Id
    private String id;  // ObjectId as String
    
    @Indexed
    private String name;
    
    @Indexed
    private String role;
    
    @Indexed(unique = true)
    private String email;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
}
```

**Impact Assessment:**
- **Annotations**: Change from JPA to MongoDB annotations
- **ID Type**: Long → String
- **Validation**: Move from database constraints to application-level validation
- **Indexing**: Manual index creation required

### 3. Repository Layer Changes (HIGH IMPACT)

#### Current PostgreSQL Repository:
```java
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByRole(String role);
    Optional<Person> findByEmail(String email);
    List<Person> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Person p WHERE p.createdAt >= :startDate")
    List<Person> findPeopleCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(p) FROM Person p WHERE p.role = :role")
    long countByRole(@Param("role") String role);
}
```

#### Required MongoDB Repository:
```java
@Repository
public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findByRole(String role);
    Optional<Person> findByEmail(String email);
    List<Person> findByNameContainingIgnoreCase(String name);
    
    @Query("{ 'createdAt' : { $gte : ?0 } }")
    List<Person> findPeopleCreatedAfter(LocalDateTime startDate);
    
    @Query(value = "{ 'role' : ?0 }", count = true)
    long countByRole(String role);
}
```

**Impact Assessment:**
- **Base Class**: JpaRepository → MongoRepository
- **ID Type**: Long → String
- **Query Syntax**: JPQL → MongoDB query syntax
- **Method Signatures**: Some methods may need parameter type changes

### 4. Response Format Changes (MEDIUM IMPACT)

#### Current PostgreSQL Response:
```json
{
  "id": 1,
  "name": "John Doe",
  "role": "Developer",
  "email": "john.doe@example.com",
  "created_at": "2025-08-07T08:09:25.368Z"
}
```

#### MongoDB Response:
```json
{
  "_id": "68945f5878997b510b64974a",
  "name": "John Doe",
  "role": "Developer",
  "email": "john.doe@example.com",
  "createdAt": "2025-08-07T08:09:25.368Z"
}
```

**Impact Assessment:**
- **Field Names**: `id` → `_id`, `created_at` → `createdAt`
- **ID Format**: Integer → String (ObjectId)
- **Client Applications**: May need updates to handle new field names

### 5. Query Parameter Changes (MEDIUM IMPACT)

#### Current PostgreSQL Queries:
```java
// Case-insensitive search
List<Person> findByNameContainingIgnoreCase(String name);

// Email lookup
Optional<Person> findByEmail(String email);

// Role-based filtering
List<Person> findByRole(String role);
```

#### MongoDB Equivalent Queries:
```java
// Case-insensitive search (requires regex)
@Query("{ 'name' : { $regex : ?0, $options: 'i' } }")
List<Person> findByNameContainingIgnoreCase(String name);

// Email lookup (same)
Optional<Person> findByEmail(String email);

// Role-based filtering (same)
List<Person> findByRole(String role);
```

**Impact Assessment:**
- **Search Implementation**: May need regex for case-insensitive search
- **Performance**: Different indexing strategies required
- **Query Complexity**: Some queries may need custom implementations

## Migration Strategy Recommendations

### 1. Backward Compatibility Approach
```java
// Option 1: Support both ID formats during transition
@GetMapping("/{id}")
public ResponseEntity<Person> getPersonById(@PathVariable String id) {
    // Try ObjectId first, then fallback to integer
    Optional<Person> person = personRepository.findById(id);
    if (!person.isPresent() && id.matches("\\d+")) {
        // Handle legacy integer ID
        person = personRepository.findByLegacyId(Long.parseLong(id));
    }
    return person.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
}
```

### 2. API Versioning Approach
```java
// Version 1 (PostgreSQL)
@GetMapping("/v1/people/{id}")
public ResponseEntity<Person> getPersonByIdV1(@PathVariable Long id)

// Version 2 (MongoDB)
@GetMapping("/v2/people/{id}")
public ResponseEntity<Person> getPersonByIdV2(@PathVariable String id)
```

### 3. Response Transformation Approach
```java
// Transform MongoDB response to match PostgreSQL format
@GetMapping("/{id}")
public ResponseEntity<PersonResponse> getPersonById(@PathVariable String id) {
    Optional<Person> person = personRepository.findById(id);
    return person.map(p -> ResponseEntity.ok(transformToLegacyFormat(p)))
                .orElse(ResponseEntity.notFound().build());
}

private PersonResponse transformToLegacyFormat(Person person) {
    return PersonResponse.builder()
        .id(person.getId())  // Keep as string
        .name(person.getName())
        .role(person.getRole())
        .email(person.getEmail())
        .createdAt(person.getCreatedAt())
        .build();
}
```

## Required Code Changes Summary

### High Priority Changes:
1. **Entity Class**: Update annotations and ID type
2. **Repository Interface**: Change base class and ID type
3. **Controller Methods**: Update ID parameter types
4. **Dependencies**: Add MongoDB starter, remove JPA starter

### Medium Priority Changes:
1. **Response Format**: Update field names and ID format
2. **Query Methods**: Update custom query implementations
3. **Validation**: Implement application-level validation
4. **Indexing**: Create MongoDB indexes

### Low Priority Changes:
1. **Configuration**: Update application.properties
2. **Testing**: Update test cases for new data format
3. **Documentation**: Update API documentation

## Risk Assessment

### High Risk:
- **Breaking Changes**: ID format changes will break existing clients
- **Data Migration**: Complex data transformation required
- **Performance**: Different indexing strategies may affect performance

### Medium Risk:
- **Query Complexity**: Some queries may need custom implementations
- **Validation**: Application-level validation vs database constraints
- **Consistency**: Eventual consistency vs ACID transactions

### Low Risk:
- **Basic CRUD**: Core operations remain similar
- **Framework**: Spring Boot supports both JPA and MongoDB
- **Testing**: Existing test patterns can be adapted
