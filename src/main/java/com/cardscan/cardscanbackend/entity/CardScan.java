package com.cardscan.cardscanbackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "card_scans")
public class CardScan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "scan_id")
    private UUID scanId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "recognized_text", columnDefinition = "TEXT")
    private String recognizedText;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference("contact-scans")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
}