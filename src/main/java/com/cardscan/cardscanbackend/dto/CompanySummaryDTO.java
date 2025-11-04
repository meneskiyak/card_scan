package com.cardscan.cardscanbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CompanySummaryDTO {
    private UUID companyId;
    private String name;
    private String website;
    private String address;
}