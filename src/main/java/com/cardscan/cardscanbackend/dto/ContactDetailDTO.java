package com.cardscan.cardscanbackend.dto;

import com.cardscan.cardscanbackend.entity.Contact;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ContactDetailDTO {

    private UUID contactId;
    private String fullName;
    private String title;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

    private CompanySummaryDTO company;
    private List<CardScanDTO> cardScans;
    private List<ContactDetailEntryDTO> details;
    private List<SocialAccountDTO> socialAccounts;
    private List<TagDTO> tags;

    public static ContactDetailDTO fromEntity(Contact contact) {
        ContactDetailDTO dto = new ContactDetailDTO();
        dto.setContactId(contact.getContactId());
        dto.setFullName(contact.getFullName());
        dto.setTitle(contact.getTitle());
        dto.setNote(contact.getNote());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());

        if (contact.getCompany() != null) {
            dto.setCompany(new CompanySummaryDTO(
                    contact.getCompany().getCompanyId(),
                    contact.getCompany().getName(),
                    contact.getCompany().getWebsite(),
                    contact.getCompany().getAddress()
            ));
        }

        dto.setCardScans(contact.getCardScans().stream()
                .map(scan -> new CardScanDTO(
                        scan.getScanId(),
                        scan.getImageUrl(),
                        scan.getRecognizedText()
                )).collect(Collectors.toList()));

        dto.setDetails(contact.getDetails().stream()
                .map(detail -> new ContactDetailEntryDTO(
                        detail.getDetailId(),
                        detail.getType(),
                        detail.getValue()
                )).collect(Collectors.toList()));

        dto.setSocialAccounts(contact.getSocialAccounts().stream()
                .map(sa -> new SocialAccountDTO(
                        sa.getSocialId(),
                        sa.getPlatformName(),
                        sa.getProfileUrl()
                )).collect(Collectors.toList()));

        dto.setTags(contact.getTags().stream()
                .map(tag -> new TagDTO(
                        tag.getTagId(),
                        tag.getName()
                )).collect(Collectors.toList()));

        return dto;
    }
}