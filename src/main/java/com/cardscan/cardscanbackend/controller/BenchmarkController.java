package com.cardscan.cardscanbackend.controller;

import com.cardscan.cardscanbackend.dto.CreateContactRequestDTO;
import com.cardscan.cardscanbackend.entity.User;
import com.cardscan.cardscanbackend.service.ContactService;
import com.cardscan.cardscanbackend.service.FirestoreService;
import com.cardscan.cardscanbackend.repository.CompanyRepository;
import com.cardscan.cardscanbackend.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final ContactService contactService; // SQL Servisi
    private final FirestoreService firestoreService; // NoSQL Servisi
    private final com.cardscan.cardscanbackend.repository.ContactRepository contactRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public BenchmarkController(ContactService contactService,
                               FirestoreService firestoreService, CompanyRepository companyRepository,
                               com.cardscan.cardscanbackend.repository.ContactRepository contactRepository) {
        this.contactService = contactService;
        this.firestoreService = firestoreService;
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
    }
    @GetMapping("/read/{id}")
    public ResponseEntity<Map<String, Object>> compareSimpleRead(@PathVariable String id) {
        Map<String, Object> results = new HashMap<>();

        // ID'yi UUID formatına çevirmeyi deneyelim (SQL için lazım)
        UUID contactId;
        try {
            contactId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz UUID formatı"));
        }

        try {
            // --- TEST 1: SQL (PostgreSQL - Index Scan) ---
            long startSQL = System.nanoTime();

            boolean sqlExists = contactRepository.findById(contactId).isPresent();

            long endSQL = System.nanoTime();
            double sqlDurationMs = (endSQL - startSQL) / 1_000_000.0;

            results.put("SQL_Found", sqlExists);
            results.put("SQL_Time_ms", sqlDurationMs);

            // --- TEST 2: NoSQL (Firebase - Direct Lookup) ---
            long startNoSQL = System.nanoTime();

            // Biz NoSQL'e kaydederken ID olarak aynı UUID stringini vermiştik
            boolean noSqlExists = firestoreService.getContactByIdNoSQL(id);

            long endNoSQL = System.nanoTime();
            double noSqlDurationMs = (endNoSQL - startNoSQL) / 1_000_000.0;

            results.put("NoSQL_Found", noSqlExists);
            results.put("NoSQL_Time_ms", noSqlDurationMs);

            // --- Sonuç ---
            results.put("Winner", sqlDurationMs < noSqlDurationMs ? "SQL" : "NoSQL");

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/update-company")
    public ResponseEntity<Map<String, Object>> compareUpdate(@RequestParam String oldName, @RequestParam String newName) {
        Map<String, Object> results = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        try {
            // --- TEST 1: SQL (PostgreSQL - Tek Satır Update) ---
            long startSQL = System.nanoTime();

            // 1. Şirketi bul
            Company company = companyRepository.findByName(oldName).orElse(null);
            int sqlCount = 0;
            if (company != null) {
                // 2. Adı güncelle ve kaydet (Sadece 1 işlem!)
                company.setName(newName);
                companyRepository.save(company);
                sqlCount = 1; // SQL'de sadece 1 kayıt güncellendi
            }

            long endSQL = System.nanoTime();
            double sqlDurationMs = (endSQL - startSQL) / 1_000_000.0;
            results.put("SQL_Updated_Rows", sqlCount); // Hep 1 olmalı
            results.put("SQL_Time_ms", sqlDurationMs);

            // --- TEST 2: NoSQL (Firebase - Çoklu Update) ---
            long startNoSQL = System.nanoTime();

            // Burada kaç tane contact varsa o kadar işlem yapılacak
            int noSqlCount = firestoreService.updateCompanyNameNoSQL(oldName, newName, currentUser.getEmail());

            long endNoSQL = System.nanoTime();
            double noSqlDurationMs = (endNoSQL - startNoSQL) / 1_000_000.0;

            results.put("NoSQL_Updated_Documents", noSqlCount); // Contact sayısı kadar olmalı
            results.put("NoSQL_Time_ms", noSqlDurationMs);

            // --- Sonuç ---
            results.put("Winner", sqlDurationMs < noSqlDurationMs ? "SQL" : "NoSQL");

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> comparePerformance(@RequestBody CreateContactRequestDTO requestDTO) {
        Map<String, Object> results = new HashMap<>();

        // Kullanıcıyı al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        try {
            // --- TEST 1: SQL (PostgreSQL) ---
            long startSQL = System.currentTimeMillis();

            contactService.createContact(requestDTO, currentUser);

            long endSQL = System.currentTimeMillis();
            long sqlDuration = endSQL - startSQL;
            results.put("SQL_PostgreSQL_Time_ms", sqlDuration);

            // --- TEST 2: NoSQL (Firebase Firestore) ---
            long startNoSQL = System.currentTimeMillis();

            firestoreService.saveContactNoSQL(requestDTO, currentUser.getEmail());

            long endNoSQL = System.currentTimeMillis();
            long noSqlDuration = endNoSQL - startNoSQL;
            results.put("NoSQL_Firebase_Time_ms", noSqlDuration);

            // --- Analiz ---
            results.put("Winner", sqlDuration < noSqlDuration ? "SQL" : "NoSQL");

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
    @GetMapping("/search/{tagName}")
    public ResponseEntity<Map<String, Object>> compareSearch(@PathVariable String tagName) {
        Map<String, Object> results = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        try {
            // --- TEST 1: SQL (PostgreSQL - JOIN) ---
            long startSQL = System.nanoTime(); // Daha hassas ölçüm için nanoTime

            int sqlCount = contactRepository.findByUserAndTags_Name(currentUser, tagName).size();

            long endSQL = System.nanoTime();
            double sqlDurationMs = (endSQL - startSQL) / 1_000_000.0;

            results.put("SQL_Result_Count", sqlCount);
            results.put("SQL_Time_ms", sqlDurationMs);

            // --- TEST 2: NoSQL (Firebase - Array Contains) ---
            long startNoSQL = System.nanoTime();

            int noSqlCount = firestoreService.searchContactByTagNoSQL(tagName, currentUser.getEmail());

            long endNoSQL = System.nanoTime();
            double noSqlDurationMs = (endNoSQL - startNoSQL) / 1_000_000.0;

            results.put("NoSQL_Result_Count", noSqlCount);
            results.put("NoSQL_Time_ms", noSqlDurationMs);

            // --- Sonuç ---
            results.put("Winner", sqlDurationMs < noSqlDurationMs ? "SQL" : "NoSQL");

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}