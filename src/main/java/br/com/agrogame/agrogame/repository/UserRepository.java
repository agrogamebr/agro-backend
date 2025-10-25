package br.com.agrogame.agrogame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agrogame.agrogame.model.User;

public interface UserRepository  extends JpaRepository<User, Integer> {

}
