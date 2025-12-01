package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.CreateContactRequestDTO;
import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FirestoreService {

    public boolean getContactByIdNoSQL(String documentId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // Doğrudan ID ile belgeye git
        ApiFuture<DocumentSnapshot> future = db.collection("contacts").document(documentId).get();

        DocumentSnapshot document = future.get();
        return document.exists();
    }

    public int updateCompanyNameNoSQL(String oldName, String newName, String userEmail) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // 1. ADIM: Eski şirket adına sahip belgeleri BUL (Read Cost)
        ApiFuture<QuerySnapshot> query = db.collection("contacts")
                .whereEqualTo("ownerEmail", userEmail)
                .whereEqualTo("organization", oldName)
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        int updateCount = 0;

        // 2. ADIM: Her bir belgeyi tek tek GÜNCELLE (Write Cost)
        for (QueryDocumentSnapshot document : documents) {
            // Sadece 'organization' alanını güncelle
            db.collection("contacts").document(document.getId())
                    .update("organization", newName);
            // Not: Gerçek hayatta burada 'Batch' işlemi kullanılır ama
            // maliyet (işlem sayısı) yine de belge sayısı kadardır.
            updateCount++;
        }

        return updateCount; // Kaç kişinin güncellendiğini dön
    }

    public int searchContactByTagNoSQL(String tagName, String userEmail) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // 1. Sorguyu Hazırla:
        // Hem kullanıcının emaili eşleşmeli HEM DE tags dizisi bu etiketi içermeli.
        // NoSQL'de bu tür "Composite Query"ler için bazen manuel index oluşturmak gerekir!
        ApiFuture<QuerySnapshot> query = db.collection("contacts")
                .whereEqualTo("ownerEmail", userEmail)
                .whereArrayContains("tags", tagName)
                .get();

        // 2. Sonuç sayısını döndür (Sadece performans ölçüyoruz, veriyi dönmeye gerek yok)
        return query.get().size();
    }

    public String saveContactNoSQL(CreateContactRequestDTO requestDTO, String userEmail) throws Exception {

        // 1. Firebase Firestore bağlantısını al
        Firestore db = FirestoreClient.getFirestore();

        String uniqueId = UUID.randomUUID().toString();

        // 2. Veriyi NoSQL formatına (İç içe JSON / Map) hazırla
        // SQL'deki gibi ayrı tablolar yok, her şey tek bir JSON'un içinde (Embedding)
        Map<String, Object> contactMap = new HashMap<>();
        GeminiExtractionResult data = requestDTO.getConfirmedData();

        contactMap.put("id", uniqueId);
        contactMap.put("ownerEmail", userEmail); // İlişki yerine e-posta yazıyoruz (Denormalization)
        contactMap.put("fullName", data.getFullName());
        contactMap.put("title", data.getTitle());
        contactMap.put("organization", data.getOrganization()); // Ayrı tablo değil, string
        contactMap.put("note", requestDTO.getNote());

        // Listeler doğrudan JSON array olarak saklanır
        contactMap.put("phones", data.getPhones());
        contactMap.put("emails", data.getEmails());
        contactMap.put("websites", data.getWebsites());
        contactMap.put("tags", data.getTags()); // Ayrı tablo değil, array

        // Görüntü URL'i
        contactMap.put("imageUrl", requestDTO.getImageUrl());

        // 3. Firestore'a "contacts" koleksiyonuna ekle
        // Bu işlem asenkrondur, sonucunu bekliyoruz (.get())
        ApiFuture<WriteResult> future = db.collection("contacts")
                .document(uniqueId)
                .set(contactMap);

        return future.get().getUpdateTime().toString();
    }
}