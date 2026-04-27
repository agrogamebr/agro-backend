-- V2__migrate_worker_production_unit_assignments_to_worker_farm_assignments.sql

INSERT INTO worker_farm_assignments (
    worker_id,
    farm_id,
    is_active,
    created_at,
    updated_at,
    updated_by
)
SELECT
    src.worker_id,
    src.farm_id,
    TRUE,
    COALESCE(src.created_at, NOW()),
    NULL,
    NULL
FROM (
    SELECT
        wpu.worker_id,
        MIN(pu.farm_id) AS farm_id,
        MIN(wpu.created_at) AS created_at
    FROM worker_production_unit_assignments wpu
    JOIN production_units pu
        ON pu.id = wpu.production_unit_id
    WHERE wpu.is_active = TRUE
    GROUP BY wpu.worker_id
    HAVING COUNT(DISTINCT pu.farm_id) = 1
) src
WHERE NOT EXISTS (
    SELECT 1
    FROM worker_farm_assignments wfa
    WHERE wfa.worker_id = src.worker_id
      AND wfa.is_active = TRUE
);