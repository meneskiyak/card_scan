package com.cardscan.cardscanbackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;
@Data @AllArgsConstructor
public class SocialAccountDTO {
    private UUID socialId;
    private String platformName;
    private String profileUrl;
}