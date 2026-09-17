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
	
	List<UserEntity> findBySchoolIdOrderByNameAsc(@Param("schoolId") Long schoolId);

	boolean existsByUsernameAndSchoolId(@Param("username") String username, @Param("schoolId") Long schoolId);
}