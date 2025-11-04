package com.cardscan.cardscanbackend.repository;

import com.cardscan.cardscanbackend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    // 🔥 Şirketin var olup olmadığını kontrol etmek için KRİTİK metot
    Optional<Company> findByName(String name);
}