package com.cardscan.cardscanbackend.service;

// 🔥 GÜNCELLEMELER
import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.entity.User;
import com.cardscan.cardscanbackend.repository.UserRepository;
// ---
import com.google.cloud.spring.vision.CloudVisionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class OcrService {

    private final CloudVisionTemplate cloudVisionTemplate;
    private final GeminiExtractionService geminiService;
    private final ContactService contactService; // 🔥 YENİ
    private final UserRepository userRepository; // 🔥 YENİ (Mock User için)

    @Autowired
    public OcrService(CloudVisionTemplate cloudVisionTemplate,
                      GeminiExtractionService geminiService,
                      ContactService contactService, // 🔥 YENİ
                      UserRepository userRepository) { // 🔥 YENİ
        this.cloudVisionTemplate = cloudVisionTemplate;
        this.geminiService = geminiService;
        this.contactService = contactService;
        this.userRepository = userRepository;
    }

    /**
     * 🔥 DÖNÜŞ TİPİ 'Contact' OLARAK GÜNCELLENDİ
     */
    public Contact processCardWithNer(MultipartFile file) throws Exception {
        Resource imageResource = file.getResource();
        String detectedText = cloudVisionTemplate.extractTextFromImage(imageResource);

        if (detectedText == null || detectedText.isEmpty() || detectedText.contains("Resimde metin algılanamadı")) {
            // Hata yönetimi (Ham metin boşsa bile bir Contact oluşturabiliriz
            // ama şimdilik fırlatmak daha doğru)
            throw new IOException("Resimde metin algılanamadı.");
        }

        // 1. Ham metni Gemini'a gönder -> DTO al
        GeminiExtractionResult resultDto = geminiService.extractEntities(detectedText);

        // 2. 🔥 YENİ: Mock verileri oluştur (Auth ve File Storage gelene kadar)

        // TODO: Spring Security/Firebase entegrasyonu gelince bu kod değişmeli.
        // Veritabanında "test@example.com" email'ine sahip bir User olmalı.
        User currentUser = userRepository.findByEmail("test@example.com")
                .orElseThrow(() -> new RuntimeException("Lütfen 'test@example.com' email'i ile bir 'User' oluşturun."));

        // TODO: Google Cloud Storage/S3 entegrasyonu gelince bu kod değişmeli.
        // Şimdilik dosya adını kullanıyoruz.
        String imageUrl = "uploads/mock/" + file.getOriginalFilename();

        // 3. 🔥 YENİ: DTO'yu, User'ı ve diğer bilgileri ContactService'e gönder
        Contact savedContact = contactService.createContactFromExtraction(
                resultDto,
                currentUser,
                detectedText,
                imageUrl
        );

        return savedContact; // Kaydedilmiş Entity'yi Controller'a döndür
    }
}