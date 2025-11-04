package com.cardscan.cardscanbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
// JSON'da olup burada olmayan alanları (örn: Gemini'ın eklediği) yok say
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiExtractionResult {

    private String fullName;
    private String title;
    private String organization;

    private List<String> phones;
    private List<String> emails;
    private List<String> websites; // Hem kişisel hem kurumsal olabilir buraya BAKILACAKKKKKK!!!!
    private List<String> addresses;

    private List<SocialMediaAccount> socialMedia;
    private List<String> tags;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SocialMediaAccount {
        private String platform;
        private String url;
    }
}