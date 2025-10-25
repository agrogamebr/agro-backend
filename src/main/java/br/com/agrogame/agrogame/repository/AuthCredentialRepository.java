package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.AuthCredential;

public interface AuthCredentialRepository  extends JpaRepository<AuthCredential, Integer> {
	Optional<AuthCredential> findByIdentifier(String identifier);
}
