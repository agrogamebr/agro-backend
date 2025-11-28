package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {
	List<Activity> findByCompanyId(Integer companyId);

	List<Activity> findByCompanyIdAndActivityStatus_Code(Integer companyId, String statusCode);
}
