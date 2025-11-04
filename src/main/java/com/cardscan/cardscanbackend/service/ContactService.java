package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.ContactDetailDTO;
import com.cardscan.cardscanbackend.dto.CreateContactRequestDTO;
import com.cardscan.cardscanbackend.dto.ContactSummaryDTO;
import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.cardscan.cardscanbackend.entity.*;
import com.cardscan.cardscanbackend.repository.CompanyRepository;
import com.cardscan.cardscanbackend.repository.ContactRepository;
import com.cardscan.cardscanbackend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final TagRepository tagRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository,
                          CompanyRepository companyRepository,
                          TagRepository tagRepository) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Contact createContact(CreateContactRequestDTO requestDTO, User currentUser) {

        if (currentUser == null) {
            throw new IllegalArgumentException("User null olamaz!");
        }

        GeminiExtractionResult dto = requestDTO.getConfirmedData();
        String imageUrl = requestDTO.getImageUrl();
        String rawText = requestDTO.getRawText();

        Company company = null;
        if (dto.getOrganization() != null && !dto.getOrganization().isEmpty()) {
            company = companyRepository.findByName(dto.getOrganization())
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setName(dto.getOrganization());
                        if (dto.getWebsites() != null && !dto.getWebsites().isEmpty()) {
                            newCompany.setWebsite(dto.getWebsites().get(0));
                        }
                        return companyRepository.save(newCompany);
                    });
        }


        Set<Tag> tags = new HashSet<>();
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
        }


        Contact contact = new Contact();
        contact.setFullName(dto.getFullName());
        contact.setTitle(dto.getTitle());
        contact.setUser(currentUser);
        contact.setCompany(company);
        contact.setTags(tags);


        CardScan scan = new CardScan();
        scan.setImageUrl(imageUrl);
        scan.setRecognizedText(rawText);
        scan.setContact(contact);
        contact.getCardScans().add(scan);

        if (dto.getPhones() != null) {
            for (String phone : dto.getPhones()) {
                addContactDetail(contact, ContactDetailType.PHONE, phone);
            }
        }
        if (dto.getEmails() != null) {
            for (String email : dto.getEmails()) {
                addContactDetail(contact, ContactDetailType.EMAIL, email);
            }
        }
        if (dto.getWebsites() != null) {
            for (String website : dto.getWebsites()) {
                addContactDetail(contact, ContactDetailType.WEBSITE, website);
            }
        }
        if (dto.getAddresses() != null) {
            for (String address : dto.getAddresses()) {
                addContactDetail(contact, ContactDetailType.ADDRESS, address);
            }
        }

        if (dto.getSocialMedia() != null) {
            for (GeminiExtractionResult.SocialMediaAccount socialDto : dto.getSocialMedia()) {
                SocialAccount sa = new SocialAccount();
                sa.setPlatformName(socialDto.getPlatform());
                sa.setProfileUrl(socialDto.getUrl());
                sa.setContact(contact);
                contact.getSocialAccounts().add(sa);
            }
        }

        return contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactSummaryDTO> getContactsForUser(User user) {

        List<Contact> contacts = contactRepository.findByUser(user);

        return contacts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactDetailDTO getContactDetails(UUID contactId, User currentUser) {

        Contact contact = contactRepository.findByContactIdAndUser(contactId, currentUser)
                .orElseThrow(() -> new RuntimeException("Contact not found or does not belong to user"));
        // TODO: 'RuntimeException' yerine özel bir 'ResourceNotFoundException' fırlatmak daha iyidir.

        return ContactDetailDTO.fromEntity(contact);
    }


    private ContactSummaryDTO convertToSummaryDTO(Contact contact) {
        ContactSummaryDTO dto = new ContactSummaryDTO();
        dto.setContactId(contact.getContactId());
        dto.setFullName(contact.getFullName());
        dto.setTitle(contact.getTitle());

        if (contact.getCompany() != null) {
            dto.setOrganizationName(contact.getCompany().getName());
        } else {
            dto.setOrganizationName(null);
        }

        return dto;
    }

    private void addContactDetail(Contact contact, ContactDetailType type, String value) {
        ContactDetail detail = new ContactDetail();
        detail.setType(type);
        detail.setValue(value);
        detail.setContact(contact);
        contact.getDetails().add(detail);
    }
}