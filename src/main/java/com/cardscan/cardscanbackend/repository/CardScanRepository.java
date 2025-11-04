package com.cardscan.cardscanbackend.repository;

import com.cardscan.cardscanbackend.entity.CardScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardScanRepository extends JpaRepository<CardScan, UUID> {
}