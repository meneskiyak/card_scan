package com.cardscan.cardscanbackend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ContactSummaryDTO {

    private UUID contactId;
    private String fullName;
    private String title;

    private String organizationName;
}