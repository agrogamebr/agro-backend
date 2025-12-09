package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserPointTransactionSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointTransactionSourceTypeRepository
		extends JpaRepository<UserPointTransactionSourceType, Integer> {
	Optional<UserPointTransactionSourceType> findByCode(String code);
}
