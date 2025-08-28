CREATE TABLE terms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    terms_of_service_agreed BOOLEAN NOT NULL DEFAULT FALSE,
    privacy_policy_agreed BOOLEAN NOT NULL DEFAULT FALSE,
    is_over_14 BOOLEAN NOT NULL DEFAULT FALSE,
    event_push_agreed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_terms_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT uk_terms_member_id UNIQUE (member_id)
);
