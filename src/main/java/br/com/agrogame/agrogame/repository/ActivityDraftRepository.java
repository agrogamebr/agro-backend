package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.ActivityDraft;

@Repository
public interface ActivityDraftRepository extends JpaRepository<ActivityDraft, Integer> {

	Optional<ActivityDraft> findByActivityId(Integer activityId);

	void deleteByActivityId(Integer activityId);
	
}
