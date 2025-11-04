package com.cardscan.cardscanbackend.dto;

import com.cardscan.cardscanbackend.entity.ContactDetailType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ContactDetailEntryDTO {
    private UUID detailId;
    private ContactDetailType type;
    private String value;
}