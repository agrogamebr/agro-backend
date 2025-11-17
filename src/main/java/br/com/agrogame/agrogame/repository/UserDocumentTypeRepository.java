package br.com.agrogame.agrogame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.agrogame.agrogame.model.UserDocumentType;

@Repository
public interface UserDocumentTypeRepository extends JpaRepository<UserDocumentType, Integer> {
    
    Optional<UserDocumentType> findByName(String name);
    
    @Query("SELECT udt FROM UserDocumentType udt WHERE (:isActive IS NULL OR udt.isActive = :isActive)")
    List<UserDocumentType> findAllByIsActive(@Param("isActive") Boolean isActive);

}
