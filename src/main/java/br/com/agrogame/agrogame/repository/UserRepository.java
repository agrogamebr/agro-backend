package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO;
import br.com.agrogame.agrogame.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByEmail1(String email1);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userType WHERE u.email1 = :email")
	Optional<User> findByEmail1WithUserType(@Param("email") String email);

	boolean existsByEmail1(String email);

	@Query("SELECT u FROM User u JOIN FETCH u.userType WHERE u.id = :id")
	Optional<User> findByIdWithUserType(@Param("id") Integer id);

	@Query(value = """
			SELECT new br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO(
			    u.id,
			    u.fullName,
			    u.email1,
			    ut.id,
			    ut.name,
			    d.documentNumber,
			    dt.id,
			    dt.name,
			    f.id,
			    f.name,
			    u.phone
			)
			FROM User u
			LEFT JOIN UserDocument d ON d.user.id = u.id AND d.isPrimary = true AND d.isActive = true
			LEFT JOIN d.documentType dt
			LEFT JOIN u.userType ut
			LEFT JOIN Farm f ON f.owner.id = u.id AND f.isActive = true
			WHERE u.company.id = :companyId
			  AND (:userTypeId IS NULL OR ut.id = :userTypeId)
			  AND (:userId IS NULL OR u.id = :userId)
			  AND (:nameLike IS NULL OR lower(u.fullName) LIKE lower(cast(:nameLike as text)))
			""", countQuery = """
			SELECT count(u)
			FROM User u
			LEFT JOIN u.userType ut
			WHERE u.company.id = :companyId
			  AND (:userTypeId IS NULL OR ut.id = :userTypeId)
			  AND (:userId IS NULL OR u.id = :userId)
			  AND (:nameLike IS NULL OR lower(u.fullName) LIKE lower(cast(:nameLike as text)))
			""")
	Page<BackofficeEmployeeSummaryDTO> findEmployeeSummariesByFilters(@Param("companyId") Integer companyId,
			@Param("userTypeId") Integer userTypeId, @Param("userId") Integer userId,
			@Param("nameLike") String nameLike, Pageable pageable);

}
