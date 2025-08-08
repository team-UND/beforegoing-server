CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL DEFAULT '워리',
    kakao_id VARCHAR(255),
    apple_id VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
	CONSTRAINT uk_kakao_id UNIQUE (kakao_id),
    CONSTRAINT uk_apple_id UNIQUE (apple_id)
);
