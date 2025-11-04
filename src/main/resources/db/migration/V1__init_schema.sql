CREATE TYPE contactdetailtype AS ENUM ('PHONE', 'EMAIL', 'WEBSITE', 'ADDRESS');

-- Users (Uygulama Kullanıcıları)
CREATE TABLE users
(
    user_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internal_id   TEXT UNIQUE,
    full_name     TEXT,
    email         TEXT UNIQUE NOT NULL,
    phone_number  TEXT,
    password_hash TEXT        NOT NULL,
    is_premium    BOOLEAN          DEFAULT false,
    created_at    TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at    TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc')
);

-- Companies (Kartvizit Şirketleri)
CREATE TABLE companies
(
    company_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL,
    website    TEXT,
    address    TEXT,
    created_at TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc')
);


-- Contacts (Kartvizit Sahipleri)
CREATE TABLE contacts
(
    contact_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    company_id UUID REFERENCES companies (company_id) ON DELETE SET NULL,
    full_name  TEXT NOT NULL,
    title      TEXT,
    created_at TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE INDEX idx_contacts_user_id ON contacts (user_id);
CREATE INDEX idx_contacts_company_id ON contacts (company_id);


-- Card Scans (Taranan Görüntüler ve OCR Metni)
CREATE TABLE card_scans
(
    scan_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id      UUID NOT NULL REFERENCES contacts (contact_id) ON DELETE CASCADE,
    image_url       TEXT NOT NULL,
    recognized_text TEXT,
    created_at      TIMESTAMPTZ      DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_card_scans_contact_id ON card_scans (contact_id);


-- Contact Details (Telefon, Email, Adres vb.)
CREATE TABLE contact_details
(
    detail_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID              NOT NULL REFERENCES contacts (contact_id) ON DELETE CASCADE,
    type       contactdetailtype NOT NULL,
    value      TEXT              NOT NULL
);
CREATE INDEX idx_contact_details_contact_id ON contact_details (contact_id);

-- Social Accounts (LinkedIn, Twitter vb.)
CREATE TABLE social_accounts
(
    social_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id    UUID        NOT NULL REFERENCES contacts (contact_id) ON DELETE CASCADE,
    platform_name VARCHAR(50) NOT NULL,
    profile_url   TEXT        NOT NULL
);
CREATE INDEX idx_social_accounts_contact_id ON social_accounts (contact_id);

-- Tags (Etiketler - #teknoloji, #finans)
CREATE TABLE tags
(
    tag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name   VARCHAR(100) UNIQUE NOT NULL
);
CREATE INDEX idx_tags_name ON tags (name);

-- Contact <-> Tag (Çoka Çok İlişki Tablosu)
CREATE TABLE contact_tag
(
    contact_id UUID NOT NULL REFERENCES contacts (contact_id) ON DELETE CASCADE,
    tag_id     UUID NOT NULL REFERENCES tags (tag_id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, tag_id)
);
