package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.CompanyDocument;

public interface CompanyDocumentsRepository extends JpaRepository<CompanyDocument, Integer> {
	List<CompanyDocument> findAllByCompanyId(Integer companyId);

	boolean existsByDocumentNumberAndDocumentType_Code(String documentNumber, String code);

	@Query("SELECT cd FROM CompanyDocument cd " + "JOIN cd.company c " + "WHERE cd.documentNumber = :documentNumber "
			+ "AND LOWER(cd.documentType.code) = LOWER(:documentTypeCode) " + "AND c.companyStatus.id = 1")
	Optional<CompanyDocument> findByDocumentNumberAndDocumentType_CodeAndCompanyApproved(
			@Param("documentNumber") String documentNumber, @Param("documentTypeCode") String documentTypeCode);

	Optional<CompanyDocument> findFirstByCompanyIdAndDocumentType_Code(Integer companyId, String documentTypeCode);
}
