package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Integer> {
    Optional<UserDocument> findByUserIdAndDocumentTypeId(Integer userId, Integer documentTypeId);
    boolean existsByDocumentNumberAndIsActiveTrue(String documentNumber);
}
