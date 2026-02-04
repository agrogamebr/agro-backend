package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.CropType;

public interface CropTypeRepository extends JpaRepository<CropType, Integer> {

	@Query("""
			SELECT DISTINCT c
			FROM CropType c
			JOIN FarmCrop fc ON fc.cropType = c
			JOIN Farm f ON f = fc.farm
			WHERE f.company.id = :companyId
			  AND c.isActive = true
			ORDER BY c.name
			""")
	List<CropType> findDistinctByCompanyId(@Param("companyId") Integer companyId);
}
