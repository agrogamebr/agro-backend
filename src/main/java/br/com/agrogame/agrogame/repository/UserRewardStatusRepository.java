package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserRewardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRewardStatusRepository extends JpaRepository<UserRewardStatus, Integer> {
	Optional<UserRewardStatus> findByCode(String code);
}
