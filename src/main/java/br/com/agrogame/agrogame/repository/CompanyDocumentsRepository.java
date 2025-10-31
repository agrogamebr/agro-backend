package br.com.agrogame.agrogame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.CompanyDocument;

public interface CompanyDocumentsRepository extends JpaRepository<CompanyDocument, Integer>{
	List<CompanyDocument> findAllByCompanyId(Integer companyId);
	boolean existsByDocumentNumberAndDocumentType_Code(String documentNumber, String code);
}
