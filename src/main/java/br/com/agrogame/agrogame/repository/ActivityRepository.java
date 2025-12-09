package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityReward;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {
	List<Activity> findByCompanyId(Integer companyId);

	List<Activity> findByCompanyIdAndActivityStatus_Code(Integer companyId, String statusCode);

	// Query para buscar atividades que têm intersecção de crop_types com a farm
	@Query("SELECT DISTINCT a FROM Activity a " + "JOIN ActivityCropType act ON act.activity.id = a.id "
			+ "JOIN FarmCrop fc ON fc.cropType.id = act.cropType.id " + "WHERE fc.farm.id = :farmId "
			+ "AND a.company.id = :companyId " + "AND a.activityStatus.id = 3")
	List<Activity> findActivitiesForFarm(@Param("farmId") Integer farmId, @Param("companyId") Integer companyId);

	@Query("SELECT ar FROM ActivityReward ar WHERE ar.activity.id = :activityId")
	List<ActivityReward> findByActivityId(@Param("activityId") Integer activityId);

}
