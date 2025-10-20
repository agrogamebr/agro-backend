package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.CompanyDocumentType;

public interface CompanyDocumentTypeRepository extends JpaRepository<CompanyDocumentType, Integer> {
    Optional<CompanyDocumentType> findByCode(String code);
}
