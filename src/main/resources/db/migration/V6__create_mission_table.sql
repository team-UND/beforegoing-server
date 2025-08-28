CREATE TABLE mission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    content VARCHAR(10) NOT NULL,
    is_checked BOOLEAN NOT NULL,
    mission_order INT,
    use_date DATE,
    mission_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_mission_scenario FOREIGN KEY (scenario_id) REFERENCES scenario(id) ON DELETE CASCADE,
    CONSTRAINT chk_mission_type CHECK (mission_type IN ('BASIC', 'TODAY')),
    CONSTRAINT chk_mission_order CHECK (mission_order >= 0 AND mission_order <= 10000000)
);
