package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class GeminiExtractionService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiExtractionResult extractEntities(String rawText) throws Exception {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY ortam değişkeni bulunamadı veya boş!");
        }

        // PROMPT
        String prompt = """
        Aşağıdaki metin bir kartvizitten OCR (optik karakter tanıma) ile çıkarılmıştır.
        Bu metni incele ve belirtilen JSON şemasına uygun olarak yapılandırılmış JSON formatında çıktı üret.
        
        Kurallar:
        - Metinden sosyal medya hesaplarını (linkedin, github, twitter vb.) bul ve 'socialMedia' listesine ekle.
        - Kartvizitin genel konusuna (örn: teknoloji, finans, sağlık) göre 1-3 adet 'tag' (etiket) oluştur. Ancak bu etiketleri belirgin etiketler oalcak şekilde ayarla.
        - Tüm telefon, e-posta, adres ve web sitelerini ilgili listelere ekle.

        - 'organization' (şirket adı) alanı çok önemlidir.
        - Şirket adı, kişi adından (fullName) ayrı bir yerde olabilir (örn: metnin sonunda, bir logonun altında).
        - 'Enerji', 'Holding', 'Teknoloji', 'Ltd.', 'A.Ş.' gibi kurumsal ifadeler içeren veya metindeki diğer kişisel bilgilerden (telefon, email) ayrı duran marka adını bul ve 'organization' alanına ata.
        - Kişi adlarını (örn: 'Elif Alemdar') 'organization' alanına atama.

        - Çıktı sadece ve sadece istenen JSON formatında olmalıdır.
        
        Metin:
        """ + rawText;

        Map<String, Object> schema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "fullName", Map.of("type", "STRING", "description", "Kişinin tam adı ve soyadı."),
                        "title", Map.of("type", "STRING", "description", "Kişinin unvanı (örn: Yazılım Geliştirici)."),
                        "organization", Map.of("type", "STRING", "description", "Kişinin çalıştığı şirket veya kurum adı."),

                        "phones", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "Bulunan tüm telefon numaraları."
                        ),
                        "emails", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "Bulunan tüm e-posta adresleri."
                        ),
                        "websites", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "Bulunan tüm web siteleri (şirket veya kişisel)."
                        ),
                        "addresses", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "Bulunan tüm fiziksel adresler."
                        ),

                        "socialMedia", Map.of(
                                "type", "ARRAY",
                                "description", "Bulunan sosyal medya hesapları.",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "platform", Map.of("type", "STRING", "description", "Platform adı (örn: linkedin, github)."),
                                                "url", Map.of("type", "STRING", "description", "Profil URL'si veya kullanıcı adı.")
                                        )
                                )
                        ),

                        "tags", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "Kartvizitin içeriğine göre önerilen etiketler (örn: teknoloji, finans)."
                        )
                ),
                "required", List.of("fullName")
        );

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schema
        );

        // API’ye gönderilecek JSON gövdesi
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                },
                "generationConfig", generationConfig
        );


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                geminiApiUrl, HttpMethod.POST, entity, String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());

        if (!root.has("candidates") || root.path("candidates").isEmpty()) {
            System.err.println("Gemini API'den 'candidates' alanı gelmedi. Yanıt: " + response.getBody());
            throw new IOException("Gemini yanıtı işlenemedi (örn. güvenlik filtresi).");
        }

        String jsonText = root
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText(null);

        if (jsonText == null) {
            throw new IOException("Gemini yanıtının yapısı bozuk. 'text' alanı bulunamadı.");
        }

        GeminiExtractionResult result = objectMapper.readValue(jsonText, GeminiExtractionResult.class);

        return result;
    }
}