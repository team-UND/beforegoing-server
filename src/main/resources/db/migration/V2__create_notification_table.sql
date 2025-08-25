CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_active BOOLEAN NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    notification_method_type VARCHAR(20),
    days_of_week VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('TIME', 'LOCATION')),
    CONSTRAINT chk_notification_method_type CHECK (notification_method_type IN ('PUSH', 'ALARM'))
);
