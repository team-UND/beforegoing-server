CREATE TABLE scenario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    scenario_name VARCHAR(10) NOT NULL,
    memo VARCHAR(15),
    scenario_order INT NOT NULL,
    notification_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_scenario_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_scenario_notification FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE CASCADE,
    CONSTRAINT uk_scenario_notification UNIQUE (notification_id),
    CONSTRAINT chk_scenario_order CHECK (scenario_order >= 0 AND scenario_order <= 10000000)
);
