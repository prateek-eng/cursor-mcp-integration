package com.example.demo.controller;

import com.example.demo.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/migration")
@CrossOrigin(origins = "*")
public class MigrationController {
    
    private final MigrationService migrationService;
    
    @Autowired
    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }
    
    /**
     * Migrate all data from PostgreSQL to MongoDB
     */
    @PostMapping("/migrate-all")
    public ResponseEntity<MigrationService.MigrationResult> migrateAllData() {
        MigrationService.MigrationResult result = migrationService.migrateAllData();
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Migrate a single person by PostgreSQL ID
     */
    @PostMapping("/migrate/{postgresId}")
    public ResponseEntity<MigrationService.MigrationResult> migratePersonById(@PathVariable Long postgresId) {
        MigrationService.MigrationResult result = migrationService.migratePersonById(postgresId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Verify migration by comparing data between PostgreSQL and MongoDB
     */
    @GetMapping("/verify")
    public ResponseEntity<MigrationService.MigrationVerificationResult> verifyMigration() {
        MigrationService.MigrationVerificationResult result = migrationService.verifyMigration();
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Rollback migration by removing migrated records from MongoDB
     */
    @PostMapping("/rollback")
    public ResponseEntity<MigrationService.MigrationResult> rollbackMigration() {
        MigrationService.MigrationResult result = migrationService.rollbackMigration();
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Get migration status and statistics
     */
    @GetMapping("/status")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        MigrationService.MigrationVerificationResult verification = migrationService.verifyMigration();
        
        MigrationStatus status = new MigrationStatus();
        status.setPostgresCount(verification.getPostgresCount());
        status.setMongoCount(verification.getMongoCount());
        status.setMigratedCount(verification.getMigratedCount());
        status.setCountsMatch(verification.isCountsMatch());
        status.setMigrationComplete(verification.getPostgresCount() == verification.getMigratedCount());
        status.setMigrationProgress(verification.getPostgresCount() > 0 ? 
            (double) verification.getMigratedCount() / verification.getPostgresCount() * 100 : 0);
        
        return ResponseEntity.ok(status);
    }
    
    // Inner class for migration status
    public static class MigrationStatus {
        private long postgresCount;
        private long mongoCount;
        private long migratedCount;
        private boolean countsMatch;
        private boolean migrationComplete;
        private double migrationProgress;
        
        // Getters and setters
        public long getPostgresCount() { return postgresCount; }
        public void setPostgresCount(long postgresCount) { this.postgresCount = postgresCount; }
        
        public long getMongoCount() { return mongoCount; }
        public void setMongoCount(long mongoCount) { this.mongoCount = mongoCount; }
        
        public long getMigratedCount() { return migratedCount; }
        public void setMigratedCount(long migratedCount) { this.migratedCount = migratedCount; }
        
        public boolean isCountsMatch() { return countsMatch; }
        public void setCountsMatch(boolean countsMatch) { this.countsMatch = countsMatch; }
        
        public boolean isMigrationComplete() { return migrationComplete; }
        public void setMigrationComplete(boolean migrationComplete) { this.migrationComplete = migrationComplete; }
        
        public double getMigrationProgress() { return migrationProgress; }
        public void setMigrationProgress(double migrationProgress) { this.migrationProgress = migrationProgress; }
    }
}
