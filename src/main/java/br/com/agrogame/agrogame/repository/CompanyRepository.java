package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
	@Query("SELECT c FROM Company c JOIN FETCH c.companyStatus JOIN FETCH c.companyType")
	List<Company> findAllWithStatusAndType();
    boolean existsByEmail1(String email);
}
