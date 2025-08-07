package com.example.demo.controller;

import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/people")
@CrossOrigin(origins = "*")
public class PersonController {
    
    private final PersonRepository personRepository;
    
    @Autowired
    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }
    
    // GET all people
    @GetMapping
    public ResponseEntity<List<Person>> getAllPeople() {
        List<Person> people = personRepository.findAll();
        return ResponseEntity.ok(people);
    }
    
    // GET person by ID
    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        Optional<Person> person = personRepository.findById(id);
        return person.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // POST create new person
    @PostMapping
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        person.setCreatedAt(LocalDateTime.now());
        Person savedPerson = personRepository.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
    }
    
    // PUT update person
    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @RequestBody Person personDetails) {
        Optional<Person> personOptional = personRepository.findById(id);
        
        if (personOptional.isPresent()) {
            Person person = personOptional.get();
            person.setName(personDetails.getName());
            person.setRole(personDetails.getRole());
            person.setEmail(personDetails.getEmail());
            
            Person updatedPerson = personRepository.save(person);
            return ResponseEntity.ok(updatedPerson);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE person
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET people by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Person>> getPeopleByRole(@PathVariable String role) {
        List<Person> people = personRepository.findByRole(role);
        return ResponseEntity.ok(people);
    }
    
    // GET person by email
    @GetMapping("/email/{email}")
    public ResponseEntity<Person> getPersonByEmail(@PathVariable String email) {
        Optional<Person> person = personRepository.findByEmail(email);
        return person.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // GET people by name (case-insensitive search)
    @GetMapping("/search")
    public ResponseEntity<List<Person>> searchPeopleByName(@RequestParam String name) {
        List<Person> people = personRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(people);
    }
    
    // GET count by role
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Long> getCountByRole(@PathVariable String role) {
        long count = personRepository.countByRole(role);
        return ResponseEntity.ok(count);
    }
}
