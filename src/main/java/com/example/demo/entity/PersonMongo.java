package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Document(collection = "people")
public class PersonMongo {
    
    @Id
    private String id;  // MongoDB ObjectId as String
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Indexed
    private String name;
    
    @NotBlank(message = "Role is required")
    @Size(min = 1, max = 255, message = "Role must be between 1 and 255 characters")
    @Indexed
    private String role;
    
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    // Legacy PostgreSQL ID for migration compatibility
    @Indexed
    private Long postgresId;
    
    // Default constructor
    public PersonMongo() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public PersonMongo(String name, String role, String email) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with PostgreSQL ID for migration
    public PersonMongo(String name, String role, String email, Long postgresId) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.postgresId = postgresId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getPostgresId() {
        return postgresId;
    }
    
    public void setPostgresId(Long postgresId) {
        this.postgresId = postgresId;
    }
    
    @Override
    public String toString() {
        return "PersonMongo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", postgresId=" + postgresId +
                '}';
    }
}
