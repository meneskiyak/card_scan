package com.cardscan.cardscanbackend.repository;

import com.cardscan.cardscanbackend.entity.ContactDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactDetailRepository extends JpaRepository<ContactDetail, UUID> {
}