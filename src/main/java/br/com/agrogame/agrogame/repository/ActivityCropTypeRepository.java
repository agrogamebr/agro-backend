package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.dto.ActivityCropTypeProjection;
import br.com.agrogame.agrogame.model.ActivityCropType;

public interface ActivityCropTypeRepository extends JpaRepository<ActivityCropType, Integer> {
	List<ActivityCropType> findByActivityId(Integer activityId);

	void deleteByActivityId(Integer activityId);

	@Query(value = """
			    SELECT
			        act.activity_id as activityId,
			        ct.name as cropName
			    FROM activity_crop_types act
			    JOIN crop_types ct ON ct.id = act.crop_type_id
			    WHERE act.activity_id IN :activityIds
			""", nativeQuery = true)
	List<ActivityCropTypeProjection> findCropsByActivityIds(@Param("activityIds") List<Integer> activityIds);
}
