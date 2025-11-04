package com.cardscan.cardscanbackend.controller;

import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final OcrService ocrService;

    @Autowired
    public ContactController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/scan")
    public ResponseEntity<Contact> processCard(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // TODO: OcrService'i, 'test@example.com' yerine
            // Firebase'den gelen GÜVENLİK KİMLİĞİ ile çalışacak şekilde güncellemeliyiz.
            Contact resultCard = ocrService.processCardWithNer(file);

            // Başarılı bir *oluşturma* işlemi '201 Created' dönmelidir.
            return ResponseEntity.status(HttpStatus.CREATED).body(resultCard);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

}