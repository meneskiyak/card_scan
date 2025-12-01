package com.cardscan.cardscanbackend.repository;

import com.cardscan.cardscanbackend.entity.Contact;
import com.cardscan.cardscanbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByUser(User user);

    Optional<Contact> findByContactIdAndUser(UUID contactId, User user);

    List<Contact> findByUserAndTags_Name(User user, String tagName);
}