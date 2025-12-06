package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.UserPointsTransaction;

@Repository
public interface UserPointsTransactionRepository extends JpaRepository<UserPointsTransaction, Integer> {

	@Query("SELECT COALESCE(MAX(t.balanceAfter), 0) " + "FROM UserPointsTransaction t " + "WHERE t.user.id = :userId")
	Integer findLatestBalance(@Param("userId") Integer userId);

	List<UserPointsTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

	@Query("SELECT t FROM UserPointsTransaction t " + "WHERE t.user.id = :userId " + "ORDER BY t.createdAt DESC")
	List<UserPointsTransaction> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);

}
