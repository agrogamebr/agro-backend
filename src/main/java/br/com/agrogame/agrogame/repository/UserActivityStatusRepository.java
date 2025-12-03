package br.com.agrogame.agrogame.repository;

import br.com.agrogame.agrogame.model.UserActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserActivityStatusRepository extends JpaRepository<UserActivityStatus, Integer> {
    Optional<UserActivityStatus> findByCode(String code);
}
