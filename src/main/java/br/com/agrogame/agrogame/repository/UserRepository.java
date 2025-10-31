package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.model.User;

public interface UserRepository  extends JpaRepository<User, Integer> {
	Optional<User> findByEmail1(String email1);
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userType WHERE u.email1 = :email")
	Optional<User> findByEmail1WithUserType(@Param("email") String email);

}
