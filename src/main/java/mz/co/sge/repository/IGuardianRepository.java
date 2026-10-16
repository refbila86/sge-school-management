package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.GuardianEntity;

@Repository
public interface IGuardianRepository
{
	GuardianEntity save(GuardianEntity guardian);

	GuardianEntity update(GuardianEntity guardian);

	void delete(Long id);

	Optional<GuardianEntity> findById(Long id);

	Optional<GuardianEntity> findByCode(String code);

	Optional<GuardianEntity> findByUserId(Long userId);

	Optional<GuardianEntity> findWithStudents(Long id);
	
    List<GuardianEntity> findAllWithStudents();  

	List<GuardianEntity> findAll();

	List<GuardianEntity> findByAcademicYearId(Long academicYearId);

	long countAll();

	List<GuardianEntity> findBySchoolId(Long schoolId);

	List<GuardianEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
	
	public long countAllByAcademicYearId(Long academicYearId);
	
	
}
