ALTER TABLE users
    ADD COLUMN firebase_uid TEXT UNIQUE;

CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);