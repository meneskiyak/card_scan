package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.entity.User;
import com.cardscan.cardscanbackend.repository.UserRepository;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class OcrService {

    private final CloudVisionTemplate cloudVisionTemplate;
    private final GeminiExtractionService geminiService;
    private final ContactService contactService;
    private final UserRepository userRepository;
    private final Storage storage;
    private final ResourceLoader resourceLoader;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    @Autowired
    public OcrService(CloudVisionTemplate cloudVisionTemplate,
                      GeminiExtractionService geminiService,
                      ContactService contactService,
                      UserRepository userRepository,
                      Storage storage,
                      ResourceLoader resourceLoader) {
        this.cloudVisionTemplate = cloudVisionTemplate;
        this.geminiService = geminiService;
        this.contactService = contactService;
        this.userRepository = userRepository;
        this.storage = storage;
        this.resourceLoader = resourceLoader;
    }

    public Contact processCardWithNer(MultipartFile file) throws Exception {

        // 1. GCS'e Yükle
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());
        String gcsPath = "gs://" + bucketName + "/" + uniqueFileName;

        // 2. GCS üzerinden OCR yap
        Resource imageResource = resourceLoader.getResource(gcsPath);
        String detectedText = cloudVisionTemplate.extractTextFromImage(imageResource);

        if (detectedText == null || detectedText.isEmpty() || detectedText.contains("Resimde metin algılanamadı")) {
            throw new IOException("Resimde metin algılanamadı.");
        }

        // 3. Gemini'a gönder
        GeminiExtractionResult resultDto = geminiService.extractEntities(detectedText);

        // 4. Kullanıcıyı bul
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Geçerli bir kullanıcı oturumu bulunamadı.");
        }
        User currentUser = (User) authentication.getPrincipal();

        // 5. Veritabanına kaydet
        Contact savedContact = contactService.createContactFromExtraction(
                resultDto,
                currentUser,
                detectedText,
                gcsPath // Gerçek GCS yolu
        );

        return savedContact;
    }
}