package com.example.demo.controller;

import com.example.demo.entity.PersonMongo;
import com.example.demo.repository.PersonMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mongo/people")
@CrossOrigin(origins = "*")
@Validated
public class PersonMongoController {
    
    private final PersonMongoRepository personMongoRepository;
    
    @Autowired
    public PersonMongoController(PersonMongoRepository personMongoRepository) {
        this.personMongoRepository = personMongoRepository;
    }
    
    // GET all people
    @GetMapping
    public ResponseEntity<List<PersonMongo>> getAllPeople() {
        List<PersonMongo> people = personMongoRepository.findAll();
        return ResponseEntity.ok(people);
    }
    
    // GET person by ID (supports both ObjectId and legacy PostgreSQL ID)
    @GetMapping("/{id}")
    public ResponseEntity<PersonMongo> getPersonById(@PathVariable String id) {
        // Try ObjectId first
        Optional<PersonMongo> person = personMongoRepository.findById(id);
        
        // If not found and ID is numeric, try PostgreSQL ID
        if (!person.isPresent() && id.matches("\\d+")) {
            person = personMongoRepository.findByPostgresId(Long.parseLong(id));
        }
        
        return person.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // POST create new person
    @PostMapping
    public ResponseEntity<PersonMongo> createPerson(@Valid @RequestBody PersonMongo person) {
        person.setCreatedAt(LocalDateTime.now());
        PersonMongo savedPerson = personMongoRepository.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
    }
    
    // PUT update person
    @PutMapping("/{id}")
    public ResponseEntity<PersonMongo> updatePerson(@PathVariable String id, @Valid @RequestBody PersonMongo personDetails) {
        Optional<PersonMongo> personOptional = personMongoRepository.findById(id);
        
        if (personOptional.isPresent()) {
            PersonMongo person = personOptional.get();
            person.setName(personDetails.getName());
            person.setRole(personDetails.getRole());
            person.setEmail(personDetails.getEmail());
            
            PersonMongo updatedPerson = personMongoRepository.save(person);
            return ResponseEntity.ok(updatedPerson);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE person
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable String id) {
        if (personMongoRepository.existsById(id)) {
            personMongoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET people by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<PersonMongo>> getPeopleByRole(@PathVariable String role) {
        List<PersonMongo> people = personMongoRepository.findByRole(role);
        return ResponseEntity.ok(people);
    }
    
    // GET person by email
    @GetMapping("/email/{email}")
    public ResponseEntity<PersonMongo> getPersonByEmail(@PathVariable String email) {
        Optional<PersonMongo> person = personMongoRepository.findByEmail(email);
        return person.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // GET people by name (case-insensitive search)
    @GetMapping("/search")
    public ResponseEntity<List<PersonMongo>> searchPeopleByName(@RequestParam String name) {
        List<PersonMongo> people = personMongoRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(people);
    }
    
    // GET count by role
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Long> getCountByRole(@PathVariable String role) {
        long count = personMongoRepository.countByRole(role);
        return ResponseEntity.ok(count);
    }
    
    // Migration-specific endpoints
    
    // GET all people with PostgreSQL IDs (for migration verification)
    @GetMapping("/migration/postgres-ids")
    public ResponseEntity<List<PersonMongo>> getAllWithPostgresIds() {
        List<PersonMongo> people = personMongoRepository.findAllWithPostgresId();
        return ResponseEntity.ok(people);
    }
    
    // GET person by PostgreSQL ID
    @GetMapping("/migration/postgres-id/{postgresId}")
    public ResponseEntity<PersonMongo> getPersonByPostgresId(@PathVariable Long postgresId) {
        Optional<PersonMongo> person = personMongoRepository.findByPostgresId(postgresId);
        return person.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // Advanced query endpoints
    
    // GET people by role and created after date
    @GetMapping("/advanced/role/{role}/after")
    public ResponseEntity<List<PersonMongo>> getPeopleByRoleAndCreatedAfter(
            @PathVariable String role,
            @RequestParam String startDate) {
        LocalDateTime startDateTime = LocalDateTime.parse(startDate);
        List<PersonMongo> people = personMongoRepository.findByRoleAndCreatedAfter(role, startDateTime);
        return ResponseEntity.ok(people);
    }
    
    // GET people by name and role
    @GetMapping("/advanced/search")
    public ResponseEntity<List<PersonMongo>> searchPeopleByNameAndRole(
            @RequestParam String name,
            @RequestParam String role) {
        List<PersonMongo> people = personMongoRepository.findByNameContainingAndRole(name, role);
        return ResponseEntity.ok(people);
    }
    
    // GET all unique roles
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = personMongoRepository.findAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    // Text search (requires text index)
    @GetMapping("/text-search")
    public ResponseEntity<List<PersonMongo>> textSearch(@RequestParam String query) {
        List<PersonMongo> people = personMongoRepository.findByTextSearch(query);
        return ResponseEntity.ok(people);
    }
}
