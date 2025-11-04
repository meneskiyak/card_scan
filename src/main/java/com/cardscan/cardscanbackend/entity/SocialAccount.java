package com.cardscan.cardscanbackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@Entity
@Table(name = "social_accounts")
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "social_id")
    private UUID socialId;

    @Column(name = "platform_name", nullable = false, length = 50)
    private String platformName;

    @Column(name = "profile_url", nullable = false, columnDefinition = "TEXT")
    private String profileUrl;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference("contact-socials")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
}