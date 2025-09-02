ALTER TABLE mission 
ADD COLUMN parent_mission_id BIGINT NULL,
ADD CONSTRAINT fk_mission_parent_mission 
    FOREIGN KEY (parent_mission_id) REFERENCES mission(id) ON DELETE CASCADE;
