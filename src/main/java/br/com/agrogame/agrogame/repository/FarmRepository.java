package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.Farm;

public interface FarmRepository extends JpaRepository<Farm, Integer> {
	
	List<Farm> findByOwnerId(Integer ownerId);
	List<Farm> findByOwnerIdAndCompanyId(Integer ownerId, Integer companyId);
}
