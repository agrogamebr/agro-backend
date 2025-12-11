package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.Farm;

public interface FarmRepository extends JpaRepository<Farm, Integer> {
	
	List<Farm> findByOwnerId(Integer ownerId);
	List<Farm> findByOwnerIdAndCompanyId(Integer ownerId, Integer companyId);
	List<Farm> findByOwnerIdAndIsActiveTrue(Integer ownerId);

    Optional<Farm> findByIdAndOwnerIdAndIsActiveTrue(Integer id, Integer ownerId);
	Optional<Farm> findByIdAndOwnerIdAndIsActiveFalse(Integer id, Integer ownerId);
}
