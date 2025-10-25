package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.UserType;

public interface UserTypeRepository  extends JpaRepository<UserType, Integer> {
	Optional<UserType> findByCode(String code);
}
