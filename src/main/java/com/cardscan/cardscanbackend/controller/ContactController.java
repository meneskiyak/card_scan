package com.cardscan.cardscanbackend.controller;

import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            Contact resultCard = ocrService.processCardWithNer(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultCard);

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Daha iyi bir hata yanıtı döndür
            return ResponseEntity.status(500).body(null);
        }
    }
}