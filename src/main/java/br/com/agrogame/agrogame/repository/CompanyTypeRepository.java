package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.CompanyType;

public interface CompanyTypeRepository extends JpaRepository<CompanyType, Integer> {
	@Query("SELECT ct FROM CompanyType ct " + "WHERE (:isActive IS NULL OR ct.isActive = :isActive)")
	List<CompanyType> findAllByIsActive(@Param("isActive") Boolean isActive);

}
