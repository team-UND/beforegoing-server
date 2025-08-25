CREATE TABLE location_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    tracking_radius_type VARCHAR(20) NOT NULL,
    start_hour INT NOT NULL,
    start_minute INT NOT NULL,
    end_hour INT NOT NULL,
    end_minute INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_location_notification_notification FOREIGN KEY (notification_id)
        REFERENCES notification(id) ON DELETE CASCADE,
    CONSTRAINT chk_tracking_radius_type
        CHECK (tracking_radius_type IN ('M_100', 'M_500', 'KM_1', 'KM_2', 'KM_3', 'KM_4')),
    CONSTRAINT chk_start_hour CHECK (start_hour >= 0 AND start_hour <= 23),
    CONSTRAINT chk_start_minute CHECK (start_minute >= 0 AND start_minute <= 59),
    CONSTRAINT chk_end_hour CHECK (end_hour >= 0 AND end_hour <= 23),
    CONSTRAINT chk_end_minute CHECK (end_minute >= 0 AND end_minute <= 59),
    CONSTRAINT chk_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0),
    CONSTRAINT chk_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0)
);
