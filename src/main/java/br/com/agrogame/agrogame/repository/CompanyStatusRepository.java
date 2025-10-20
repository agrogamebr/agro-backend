package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.CompanyStatus;

public interface CompanyStatusRepository extends JpaRepository<CompanyStatus, Integer> {
    Optional<CompanyStatus> findByCode(String code);
}

