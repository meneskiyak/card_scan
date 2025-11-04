package com.cardscan.cardscanbackend.repository;

import com.cardscan.cardscanbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    // 🔥 Etiketin var olup olmadığını kontrol etmek için KRİTİK metot
    Optional<Tag> findByName(String name);
}