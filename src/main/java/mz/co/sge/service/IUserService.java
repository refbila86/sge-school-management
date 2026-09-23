package mz.co.sge.service;

import java.util.List;
import java.util.Optional;
import mz.co.sge.entity.UserEntity;

public interface IUserService
{

	UserEntity authenticate(String username, String rawPassword, Long schoolId);

	List<UserEntity> findAll();

	List<UserEntity> findBySchoolId(Long schoolId);

	List<UserEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	Optional<UserEntity> findById(Long id);

	UserEntity save(UserEntity user);

	void delete(Long id);

	boolean existsByUsernameAndSchoolId(String username, Long schoolId);

	boolean existsByUsernameAndSchoolIdAndIdNot(String username, Long schoolId, Long id);
}