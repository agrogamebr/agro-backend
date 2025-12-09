package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserPointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointTransactionTypeRepository extends JpaRepository<UserPointTransactionType, Integer> {
    Optional<UserPointTransactionType> findByCode(String code);
}
