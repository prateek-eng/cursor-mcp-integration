package com.example.demo.repository;

import com.example.demo.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    // Custom query methods
    List<Person> findByRole(String role);
    
    Optional<Person> findByEmail(String email);
    
    List<Person> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Person p WHERE p.createdAt >= :startDate")
    List<Person> findPeopleCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query("SELECT COUNT(p) FROM Person p WHERE p.role = :role")
    long countByRole(@Param("role") String role);
}
