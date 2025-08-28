CREATE TABLE time_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    start_hour INT NOT NULL,
    start_minute INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_time_notification_notification FOREIGN KEY (notification_id)
        REFERENCES notification(id) ON DELETE CASCADE,
    CONSTRAINT chk_time_start_hour CHECK (start_hour >= 0 AND start_hour <= 23),
    CONSTRAINT chk_time_start_minute CHECK (start_minute >= 0 AND start_minute <= 59)
);
