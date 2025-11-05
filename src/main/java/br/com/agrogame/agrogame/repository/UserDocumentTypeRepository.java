package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentTypeRepository extends JpaRepository<UserDocumentType, Integer> {
    
    Optional<UserDocumentType> findByName(String name);
}
