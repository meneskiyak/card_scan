package com.cardscan.cardscanbackend.dto;

import lombok.Data;

/**
 * Kullanıcı "Edit Page" (Düzenleme Sayfası) üzerinde
 * değişiklikleri yaptıktan ve "Save" butonuna bastıktan sonra
 * frontend'den backend'e gönderilecek olan nihai veriyi temsil eder.
 */
@Data
public class CreateContactRequestDTO {

    private GeminiExtractionResult confirmedData;
    private String imageUrl;
    private String rawText;
}