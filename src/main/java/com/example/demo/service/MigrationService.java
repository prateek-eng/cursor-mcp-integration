package com.example.demo.service;

import com.example.demo.entity.Person;
import com.example.demo.entity.PersonMongo;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.PersonMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MigrationService {
    
    private final PersonRepository personRepository;
    private final PersonMongoRepository personMongoRepository;
    
    @Autowired
    public MigrationService(PersonRepository personRepository, PersonMongoRepository personMongoRepository) {
        this.personRepository = personRepository;
        this.personMongoRepository = personMongoRepository;
    }
    
    /**
     * Migrate all data from PostgreSQL to MongoDB
     */
    @Transactional
    public MigrationResult migrateAllData() {
        MigrationResult result = new MigrationResult();
        
        try {
            // Get all people from PostgreSQL
            List<Person> postgresPeople = personRepository.findAll();
            result.setTotalRecords(postgresPeople.size());
            
            for (Person postgresPerson : postgresPeople) {
                try {
                    // Check if already migrated
                    Optional<PersonMongo> existingMongoPerson = 
                        personMongoRepository.findByPostgresId(postgresPerson.getId());
                    
                    if (existingMongoPerson.isPresent()) {
                        result.incrementSkipped();
                        continue;
                    }
                    
                    // Create MongoDB document
                    PersonMongo mongoPerson = new PersonMongo(
                        postgresPerson.getName(),
                        postgresPerson.getRole(),
                        postgresPerson.getEmail(),
                        postgresPerson.getId()  // Preserve PostgreSQL ID
                    );
                    
                    // Set creation time from PostgreSQL
                    if (postgresPerson.getCreatedAt() != null) {
                        mongoPerson.setCreatedAt(postgresPerson.getCreatedAt());
                    }
                    
                    // Save to MongoDB
                    PersonMongo savedPerson = personMongoRepository.save(mongoPerson);
                    result.incrementMigrated();
                    
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError("Failed to migrate person ID " + postgresPerson.getId() + ": " + e.getMessage());
                }
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.addError("Migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Migrate a single person by PostgreSQL ID
     */
    public MigrationResult migratePersonById(Long postgresId) {
        MigrationResult result = new MigrationResult();
        
        try {
            // Get person from PostgreSQL
            Optional<Person> postgresPerson = personRepository.findById(postgresId);
            
            if (!postgresPerson.isPresent()) {
                result.setSuccess(false);
                result.addError("Person with PostgreSQL ID " + postgresId + " not found");
                return result;
            }
            
            Person person = postgresPerson.get();
            
            // Check if already migrated
            Optional<PersonMongo> existingMongoPerson = 
                personMongoRepository.findByPostgresId(postgresId);
            
            if (existingMongoPerson.isPresent()) {
                result.setSuccess(false);
                result.addError("Person with PostgreSQL ID " + postgresId + " already migrated");
                return result;
            }
            
            // Create MongoDB document
            PersonMongo mongoPerson = new PersonMongo(
                person.getName(),
                person.getRole(),
                person.getEmail(),
                person.getId()
            );
            
            if (person.getCreatedAt() != null) {
                mongoPerson.setCreatedAt(person.getCreatedAt());
            }
            
            // Save to MongoDB
            PersonMongo savedPerson = personMongoRepository.save(mongoPerson);
            result.incrementMigrated();
            result.setTotalRecords(1);
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.addError("Migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Verify migration by comparing record counts
     */
    public MigrationVerificationResult verifyMigration() {
        MigrationVerificationResult result = new MigrationVerificationResult();
        
        try {
            // Count records in PostgreSQL
            long postgresCount = personRepository.count();
            result.setPostgresCount(postgresCount);
            
            // Count records in MongoDB
            long mongoCount = personMongoRepository.count();
            result.setMongoCount(mongoCount);
            
            // Count migrated records (with PostgreSQL ID)
            long migratedCount = personMongoRepository.findAllWithPostgresId().size();
            result.setMigratedCount(migratedCount);
            
            // Check for data consistency
            result.setCountsMatch(postgresCount == migratedCount);
            
            // Verify sample records
            List<Person> postgresSample = personRepository.findAll();
            for (Person postgresPerson : postgresSample) {
                Optional<PersonMongo> mongoPerson = 
                    personMongoRepository.findByPostgresId(postgresPerson.getId());
                
                if (mongoPerson.isPresent()) {
                    PersonMongo mongo = mongoPerson.get();
                    if (postgresPerson.getName().equals(mongo.getName()) &&
                        postgresPerson.getRole().equals(mongo.getRole()) &&
                        (postgresPerson.getEmail() == null ? mongo.getEmail() == null : 
                         postgresPerson.getEmail().equals(mongo.getEmail()))) {
                        result.incrementVerified();
                    } else {
                        result.incrementMismatched();
                    }
                } else {
                    result.incrementMissing();
                }
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.addError("Verification failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Rollback migration by removing migrated records
     */
    @Transactional
    public MigrationResult rollbackMigration() {
        MigrationResult result = new MigrationResult();
        
        try {
            List<PersonMongo> migratedPeople = personMongoRepository.findAllWithPostgresId();
            result.setTotalRecords(migratedPeople.size());
            
            for (PersonMongo mongoPerson : migratedPeople) {
                try {
                    personMongoRepository.delete(mongoPerson);
                    result.incrementMigrated(); // Using migrated field for rollback count
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError("Failed to rollback person ID " + mongoPerson.getPostgresId() + ": " + e.getMessage());
                }
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.addError("Rollback failed: " + e.getMessage());
        }
        
        return result;
    }
    
    // Inner classes for result objects
    public static class MigrationResult {
        private boolean success;
        private int totalRecords;
        private int migrated;
        private int skipped;
        private int failed;
        private List<String> errors = new java.util.ArrayList<>();
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getMigrated() { return migrated; }
        public void setMigrated(int migrated) { this.migrated = migrated; }
        
        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }
        
        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        // Helper methods
        public void incrementMigrated() { this.migrated++; }
        public void incrementSkipped() { this.skipped++; }
        public void incrementFailed() { this.failed++; }
        public void addError(String error) { this.errors.add(error); }
    }
    
    public static class MigrationVerificationResult {
        private boolean success;
        private long postgresCount;
        private long mongoCount;
        private long migratedCount;
        private boolean countsMatch;
        private int verified;
        private int mismatched;
        private int missing;
        private List<String> errors = new java.util.ArrayList<>();
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public long getPostgresCount() { return postgresCount; }
        public void setPostgresCount(long postgresCount) { this.postgresCount = postgresCount; }
        
        public long getMongoCount() { return mongoCount; }
        public void setMongoCount(long mongoCount) { this.mongoCount = mongoCount; }
        
        public long getMigratedCount() { return migratedCount; }
        public void setMigratedCount(long migratedCount) { this.migratedCount = migratedCount; }
        
        public boolean isCountsMatch() { return countsMatch; }
        public void setCountsMatch(boolean countsMatch) { this.countsMatch = countsMatch; }
        
        public int getVerified() { return verified; }
        public void setVerified(int verified) { this.verified = verified; }
        
        public int getMismatched() { return mismatched; }
        public void setMismatched(int mismatched) { this.mismatched = mismatched; }
        
        public int getMissing() { return missing; }
        public void setMissing(int missing) { this.missing = missing; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        // Helper methods
        public void incrementVerified() { this.verified++; }
        public void incrementMismatched() { this.mismatched++; }
        public void incrementMissing() { this.missing++; }
        public void addError(String error) { this.errors.add(error); }
    }
}
