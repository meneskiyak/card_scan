package com.cardscan.cardscanbackend.dto;

import lombok.Data;

@Data
public class CreateContactRequestDTO {

    private GeminiExtractionResult confirmedData;
    private String imageUrl;
    private String rawText;
    private String note;
}