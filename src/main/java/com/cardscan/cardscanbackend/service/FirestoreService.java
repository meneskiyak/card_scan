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

        ApiFuture<DocumentSnapshot> future = db.collection("contacts").document(documentId).get();

        DocumentSnapshot document = future.get();
        return document.exists();
    }

    public int updateCompanyNameNoSQL(String oldName, String newName, String userEmail) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = db.collection("contacts")
                .whereEqualTo("ownerEmail", userEmail)
                .whereEqualTo("organization", oldName)
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        int updateCount = 0;

        for (QueryDocumentSnapshot document : documents) {
            db.collection("contacts").document(document.getId())
                    .update("organization", newName);
            updateCount++;
        }

        return updateCount;
    }

    public int searchContactByTagNoSQL(String tagName, String userEmail) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = db.collection("contacts")
                .whereEqualTo("ownerEmail", userEmail)
                .whereArrayContains("tags", tagName)
                .get();

        return query.get().size();
    }

    public String saveContactNoSQL(CreateContactRequestDTO requestDTO, String userEmail) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        String uniqueId = UUID.randomUUID().toString();

        Map<String, Object> contactMap = new HashMap<>();
        GeminiExtractionResult data = requestDTO.getConfirmedData();

        contactMap.put("id", uniqueId);
        contactMap.put("ownerEmail", userEmail);
        contactMap.put("fullName", data.getFullName());
        contactMap.put("title", data.getTitle());
        contactMap.put("organization", data.getOrganization());
        contactMap.put("note", requestDTO.getNote());

        contactMap.put("phones", data.getPhones());
        contactMap.put("emails", data.getEmails());
        contactMap.put("websites", data.getWebsites());
        contactMap.put("tags", data.getTags());

        contactMap.put("imageUrl", requestDTO.getImageUrl());

        ApiFuture<WriteResult> future = db.collection("contacts")
                .document(uniqueId)
                .set(contactMap);

        return future.get().getUpdateTime().toString();
    }
}