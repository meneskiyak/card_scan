package com.cardscan.cardscanbackend.controller;

import com.cardscan.cardscanbackend.dto.ContactDetailDTO;
import com.cardscan.cardscanbackend.dto.ContactSummaryDTO;
import com.cardscan.cardscanbackend.dto.CreateContactRequestDTO;
import com.cardscan.cardscanbackend.dto.ProcessResponseDTO;
import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.entity.User;
import com.cardscan.cardscanbackend.service.ContactService;
import com.cardscan.cardscanbackend.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ContactController {

    private final OcrService ocrService;
    private final ContactService contactService;

    @Autowired
    public ContactController(OcrService ocrService, ContactService contactService) {
        this.ocrService = ocrService;
        this.contactService = contactService;
    }

    /
    @PostMapping("/scan/process-image")
    public ResponseEntity<ProcessResponseDTO> processImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // OcrService artık sadece işleme yapar
            ProcessResponseDTO responseDTO = ocrService.processImageForEditing(file);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@RequestBody CreateContactRequestDTO requestDTO) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            Contact savedContact = contactService.createContact(requestDTO, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactSummaryDTO>> getMyContacts() {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            List<ContactSummaryDTO> contacts = contactService.getContactsForUser(currentUser);

            return ResponseEntity.ok(contacts);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<ContactDetailDTO> getContactById(@PathVariable UUID contactId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            ContactDetailDTO contactDto = contactService.getContactDetails(contactId, currentUser);

            return ResponseEntity.ok(contactDto);

        } catch (RuntimeException e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}