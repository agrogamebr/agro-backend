package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuralProducerRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail1(String email);
}
