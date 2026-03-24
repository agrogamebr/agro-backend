package br.com.agrogame.agrogame.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO;
import br.com.agrogame.agrogame.dto.BackofficeProducerSummaryDTO;
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
			    u.email2,
			    ut.id,
			    ut.name,
			    d.documentNumber,
			    dt.id,
			    dt.code,
			    u.phone,
			    u.address,
			    u.number,
			    u.zipcode,
			    u.city,
			    u.state
			)
			FROM User u
			LEFT JOIN UserDocument d
			       ON d.user.id = u.id AND d.isPrimary = true AND d.isActive = true
			LEFT JOIN d.documentType dt
			LEFT JOIN u.userType ut
			WHERE u.company.id = :companyId
			  AND (:userTypeId IS NULL OR ut.id = :userTypeId)
			  AND (:userId IS NULL OR u.id = :userId)
			  AND (:nameLike IS NULL OR lower(u.fullName) LIKE lower(cast(:nameLike as string)))
			  AND (:cpfLike IS NULL OR (d.documentNumber LIKE :cpfLike AND dt.id = 1))
			  AND (:statusId IS NULL OR u.userStatus.id = :statusId)
			""", countQuery = """
			SELECT count(u)
			FROM User u
			LEFT JOIN UserDocument d
			       ON d.user.id = u.id AND d.isPrimary = true AND d.isActive = true
			LEFT JOIN d.documentType dt
			LEFT JOIN u.userType ut
			WHERE u.company.id = :companyId
			  AND (:userTypeId IS NULL OR ut.id = :userTypeId)
			  AND (:userId IS NULL OR u.id = :userId)
			  AND (:nameLike IS NULL OR lower(u.fullName) LIKE lower(cast(:nameLike as string)))
			  AND (:cpfLike IS NULL OR (d.documentNumber LIKE :cpfLike AND dt.id = 1))
			  AND (:statusId IS NULL OR u.userStatus.id = :statusId)
			""")
	Page<BackofficeEmployeeSummaryDTO> findEmployeeSummariesByFilters(@Param("companyId") Integer companyId,
			@Param("userTypeId") Integer userTypeId, @Param("userId") Integer userId,
			@Param("nameLike") String nameLike, @Param("cpfLike") String cpf, @Param("statusId") Integer statusId,
			Pageable pageable);

	@Query(value = """
			SELECT new br.com.agrogame.agrogame.dto.BackofficeProducerSummaryDTO(
			    u.id,
			    u.fullName,
			    d.documentNumber,
			    us.id,
			    us.name,
			    u.createdAt,
			    u.updatedAt,
			   COALESCE(approver.fullName, null)
			)
			FROM User u
			LEFT JOIN UserDocument d
			       ON d.user.id = u.id
			      AND d.isPrimary = true
			      AND d.isActive = true
			      AND d.documentType.id = 1
			LEFT JOIN u.userStatus us
			LEFT JOIN u.updatedBy approver
			WHERE u.company.id = :companyId
			  AND u.userType.id = 8
			  AND (:nameLike IS NULL OR LOWER(u.fullName) LIKE :nameLike)
			  AND (:cpfLike  IS NULL OR d.documentNumber LIKE :cpfLike)
			  AND (:statusId IS NULL OR us.id = :statusId)
			ORDER BY u.id ASC
			""", countQuery = """
			SELECT count(u)
			FROM User u
			LEFT JOIN UserDocument d
			       ON d.user.id = u.id
			      AND d.isPrimary = true
			      AND d.isActive = true
			      AND d.documentType.id = 1
			LEFT JOIN u.userStatus us
			WHERE u.company.id = :companyId
			  AND u.userType.id = 8
			  AND (:nameLike IS NULL OR LOWER(u.fullName) LIKE :nameLike)
			  AND (:cpfLike  IS NULL OR d.documentNumber LIKE :cpfLike)
			  AND (:statusId IS NULL OR us.id = :statusId)
			""")
	Page<BackofficeProducerSummaryDTO> listProducers(@Param("companyId") Integer companyId,
			@Param("nameLike") String nameLike, @Param("cpfLike") String cpfLike, @Param("statusId") Integer statusId,
			Pageable pageable);

}
