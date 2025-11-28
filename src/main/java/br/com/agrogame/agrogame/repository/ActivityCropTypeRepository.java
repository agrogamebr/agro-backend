package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.ActivityCropType;

public interface ActivityCropTypeRepository extends JpaRepository<ActivityCropType, Integer> {
    List<ActivityCropType> findByActivityId(Integer activityId);
    void deleteByActivityId(Integer activityId);
}
