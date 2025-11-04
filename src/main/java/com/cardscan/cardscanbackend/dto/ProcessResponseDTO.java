package com.cardscan.cardscanbackend.dto;

import lombok.Data;

@Data
public class ProcessResponseDTO {

    private GeminiExtractionResult extractedData;
    private String imageUrl;
    private String rawText;
}