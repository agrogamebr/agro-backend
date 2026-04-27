-- rollback_worker_farm_assignments_migration.sql

DELETE FROM worker_farm_assignments wfa
WHERE wfa.updated_by IS NULL
  AND EXISTS (
      SELECT 1
      FROM worker_production_unit_assignments wpu
      JOIN production_units pu
          ON pu.id = wpu.production_unit_id
      WHERE wpu.worker_id = wfa.worker_id
        AND wpu.is_active = TRUE
        AND pu.farm_id = wfa.farm_id
  );