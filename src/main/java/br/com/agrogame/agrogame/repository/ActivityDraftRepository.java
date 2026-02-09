package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ActivityDraft;

@Repository
public interface ActivityDraftRepository extends JpaRepository<ActivityDraft, Integer> {

	List<ActivityDraft> findByActivityId(Integer activityId);

	void deleteByActivityId(Integer activityId);
}
