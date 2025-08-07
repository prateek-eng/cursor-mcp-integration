package com.example.demo.repository;

import com.example.demo.entity.PersonMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonMongoRepository extends MongoRepository<PersonMongo, String> {
    
    // Basic query methods (same as PostgreSQL)
    List<PersonMongo> findByRole(String role);
    
    Optional<PersonMongo> findByEmail(String email);
    
    // Case-insensitive search using regex
    @Query("{ 'name' : { $regex : ?0, $options: 'i' } }")
    List<PersonMongo> findByNameContainingIgnoreCase(String name);
    
    // Date-based queries
    @Query("{ 'createdAt' : { $gte : ?0 } }")
    List<PersonMongo> findPeopleCreatedAfter(LocalDateTime startDate);
    
    // Count queries
    @Query(value = "{ 'role' : ?0 }", count = true)
    long countByRole(String role);
    
    // Migration compatibility methods
    Optional<PersonMongo> findByPostgresId(Long postgresId);
    
    @Query("{ 'postgresId' : { $exists : true } }")
    List<PersonMongo> findAllWithPostgresId();
    
    // Advanced queries
    @Query("{ 'role' : ?0, 'createdAt' : { $gte : ?1 } }")
    List<PersonMongo> findByRoleAndCreatedAfter(String role, LocalDateTime startDate);
    
    @Query("{ 'name' : { $regex : ?0, $options: 'i' }, 'role' : ?1 }")
    List<PersonMongo> findByNameContainingAndRole(String name, String role);
    
    // Aggregation queries
    @Query(value = "{}", fields = "{ 'role' : 1, '_id' : 0 }")
    List<String> findAllRoles();
    
    // Text search (requires text index)
    @Query("{ $text : { $search : ?0 } }")
    List<PersonMongo> findByTextSearch(String searchText);
}
