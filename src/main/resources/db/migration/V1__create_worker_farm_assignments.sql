CREATE TABLE worker_farm_assignments (
    id           SERIAL PRIMARY KEY,
    worker_id    INTEGER NOT NULL,
    farm_id      INTEGER NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    updated_by   INTEGER
);

CREATE INDEX idx_worker_farm_assignments_worker_id
    ON worker_farm_assignments (worker_id);

CREATE INDEX idx_worker_farm_assignments_farm_id
    ON worker_farm_assignments (farm_id);

ALTER TABLE worker_farm_assignments
    ADD CONSTRAINT fk_worker_farm_assignments_worker
    FOREIGN KEY (worker_id) REFERENCES users(id);

ALTER TABLE worker_farm_assignments
    ADD CONSTRAINT fk_worker_farm_assignments_farm
    FOREIGN KEY (farm_id) REFERENCES farms(id);