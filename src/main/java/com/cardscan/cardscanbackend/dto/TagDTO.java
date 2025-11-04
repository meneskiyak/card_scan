package com.cardscan.cardscanbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TagDTO {
    private UUID tagId;
    private String name;
}