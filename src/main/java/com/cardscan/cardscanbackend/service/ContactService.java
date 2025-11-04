package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.dto.GeminiExtractionResult;
import com.cardscan.cardscanbackend.entity.*; // Tüm entity'ler
import com.cardscan.cardscanbackend.repository.CompanyRepository;
import com.cardscan.cardscanbackend.repository.ContactRepository;
import com.cardscan.cardscanbackend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final TagRepository tagRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository,
                          CompanyRepository companyRepository,
                          TagRepository tagRepository) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Gelen DTO'yu alır ve ilişkisel veritabanına kaydeder.
     * Hata olursa tüm işlem geri alınır (@Transactional).
     */
    @Transactional
    public Contact createContactFromExtraction(
            GeminiExtractionResult dto,
            User user,
            String rawText,
            String imageUrl
    ) {
        if (user == null) {
            throw new IllegalArgumentException("User null olamaz!");
        }

        // 1. ŞİRKETİ BUL VEYA YARAT (findOrCreate)
        Company company = null;
        if (dto.getOrganization() != null && !dto.getOrganization().isEmpty()) {
            company = companyRepository.findByName(dto.getOrganization())
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setName(dto.getOrganization());
                        // Şirket web sitesini de DTO'dan alabiliriz
                        if (dto.getWebsites() != null && !dto.getWebsites().isEmpty()) {
                            // Genelde ilk web sitesi şirketinki olur
                            newCompany.setWebsite(dto.getWebsites().get(0));
                        }
                        return companyRepository.save(newCompany);
                    });
        }

        // 2. ETİKETLERİ BUL VEYA YARAT (findOrCreate)
        Set<Tag> tags = new HashSet<>();
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
        }

        // 3. ANA CONTACT NESNESİNİ OLUŞTUR
        Contact contact = new Contact();
        contact.setFullName(dto.getFullName());
        contact.setTitle(dto.getTitle());
        contact.setUser(user);      // 🔥 İlişkiyi kur (Hangi kullanıcıya ait)
        contact.setCompany(company);  // 🔥 İlişkiyi kur (Hangi şirkete ait)
        contact.setTags(tags);      // 🔥 İlişkiyi kur (Hangi etiketlere sahip)

        // 4. ALT NESNELERİ (ÇOCUKLARI) OLUŞTUR VE CONTACT'A BAĞLA

        // 4a. CardScan (Bu tarama işlemi)
        CardScan scan = new CardScan();
        scan.setImageUrl(imageUrl);
        scan.setRecognizedText(rawText);
        scan.setContact(contact); // Çift yönlü ilişki
        contact.getCardScans().add(scan);

        // 4b. ContactDetails (Telefon, Email vb.)
        if (dto.getPhones() != null) {
            for (String phone : dto.getPhones()) {
                addContactDetail(contact, ContactDetailType.PHONE, phone);
            }
        }
        if (dto.getEmails() != null) {
            for (String email : dto.getEmails()) {
                addContactDetail(contact, ContactDetailType.EMAIL, email);
            }
        }
        if (dto.getWebsites() != null) {
            for (String website : dto.getWebsites()) {
                addContactDetail(contact, ContactDetailType.WEBSITE, website);
            }
        }
        if (dto.getAddresses() != null) {
            for (String address : dto.getAddresses()) {
                addContactDetail(contact, ContactDetailType.ADDRESS, address);
            }
        }

        // 4c. SocialAccounts (LinkedIn, GitHub vb.)
        if (dto.getSocialMedia() != null) {
            for (GeminiExtractionResult.SocialMediaAccount socialDto : dto.getSocialMedia()) {
                SocialAccount sa = new SocialAccount();
                sa.setPlatformName(socialDto.getPlatform());
                sa.setProfileUrl(socialDto.getUrl());
                sa.setContact(contact); // Çift yönlü ilişki
                contact.getSocialAccounts().add(sa);
            }
        }

        // 5. KAYDET
        // @Transactional ve CascadeType.ALL sayesinde,
        // SADECE 'contact' nesnesini kaydetmek,
        // ona bağlı tüm 'CardScan', 'ContactDetail' ve 'SocialAccount' nesnelerini de
        // otomatik olarak veritabanına ekleyecektir.
        return contactRepository.save(contact);
    }

    // Helper metot
    private void addContactDetail(Contact contact, ContactDetailType type, String value) {
        ContactDetail detail = new ContactDetail();
        detail.setType(type);
        detail.setValue(value);
        detail.setContact(contact);
        contact.getDetails().add(detail);
    }
}