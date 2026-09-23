package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>
{

	@Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.schoolId = :schoolId")
	Optional<UserEntity> findByUsernameAndSchoolId(@Param("username") String username, @Param("schoolId") Long schoolId);

	List<UserEntity> findBySchoolIdOrderByNameAsc(Long schoolId);

	List<UserEntity> findBySchoolIdAndAcademicYearIdOrderByNameAsc(Long schoolId, Long academicYearId);

	List<UserEntity> findByAcademicYearIdOrderByNameAsc(Long academicYearId);

	boolean existsByUsernameAndSchoolId(String username, Long schoolId);

	boolean existsByUsernameAndSchoolIdAndIdNot(String username, Long schoolId, Long id);
}