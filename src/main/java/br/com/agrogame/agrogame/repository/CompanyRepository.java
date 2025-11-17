package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
	@Query("SELECT c FROM Company c JOIN FETCH c.companyStatus JOIN FETCH c.companyType")
	List<Company> findAllWithStatusAndType();
	boolean existsByEmail1(String email);
	
	@Query("SELECT c FROM Company c " +
			"JOIN FETCH c.companyStatus cs " +
			"JOIN FETCH c.companyType ct " +
			"WHERE cs.code = :code")
	List<Company> findActiveCompanies(@Param("code") String code);
	
	@Query("SELECT c FROM Company c " +
			"JOIN FETCH c.companyStatus cs " +
			"JOIN FETCH c.companyType ct " +
			"WHERE ((:status IS NULL OR :status = '') OR LOWER(cs.code) = LOWER(:status)) " +
			"AND (:companyTypeId IS NULL OR ct.id = :companyTypeId)")
	List<Company> findByStatusAndTypeFetch(@Param("status") String status,
			@Param("companyTypeId") Integer companyTypeId);

}
