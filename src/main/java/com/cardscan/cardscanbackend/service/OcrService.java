package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.cardscan.cardscanbackend.dto.ProcessResponseDTO;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class OcrService {

    private final CloudVisionTemplate cloudVisionTemplate;
    private final GeminiExtractionService geminiService;
    private final Storage storage;
    private final ResourceLoader resourceLoader;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    @Autowired
    public OcrService(CloudVisionTemplate cloudVisionTemplate,
                      GeminiExtractionService geminiService,
                      Storage storage,
                      ResourceLoader resourceLoader) {
        this.cloudVisionTemplate = cloudVisionTemplate;
        this.geminiService = geminiService;
        this.storage = storage;
        this.resourceLoader = resourceLoader;
    }

    public ProcessResponseDTO processImageForEditing(MultipartFile file) throws Exception {


        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType())
                .build();


        storage.create(blobInfo, file.getBytes());

        String gcsPath = "gs://" + bucketName + "/" + uniqueFileName;

        Resource imageResource = resourceLoader.getResource(gcsPath);
        String detectedText = cloudVisionTemplate.extractTextFromImage(imageResource);

        if (detectedText == null || detectedText.isEmpty() || detectedText.contains("Resimde metin algılanamadı")) {
            throw new IOException("Resimde metin algılanamadı.");
        }


        GeminiExtractionResult resultDto = geminiService.extractEntities(detectedText);

        ProcessResponseDTO response = new ProcessResponseDTO();
        response.setExtractedData(resultDto);
        response.setImageUrl(gcsPath);
        response.setRawText(detectedText);

        return response;
    }
}