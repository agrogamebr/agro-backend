package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.UserDocument;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Integer> {
	Optional<UserDocument> findByUserIdAndDocumentTypeId(Integer userId, Integer documentTypeId);

	boolean existsByDocumentNumberAndIsActiveTrue(String documentNumber);

	boolean existsByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);

	Optional<UserDocument> findByDocumentNumberAndIsActiveTrue(String documentNumber);

	@Query("SELECT ud FROM UserDocument ud " +
		       "WHERE ud.documentNumber = :documentNumber " +
		       "AND LOWER(ud.documentType.code) = LOWER(:documentTypeCode) " +
		       "AND ud.isActive = true")
		Optional<UserDocument> findByDocumentNumberAndDocumentType_CodeAndIsActiveTrue(@Param("documentNumber") String documentNumber, @Param("documentTypeCode") String documentTypeCode);

}
