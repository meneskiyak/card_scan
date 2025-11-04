package com.cardscan.cardscanbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;
@Data @AllArgsConstructor
public class CardScanDTO {
    private UUID scanId;
    private String imageUrl;
    private String recognizedText;
}