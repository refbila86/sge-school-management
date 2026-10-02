package mz.co.sge.repository;

import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import java.util.List;
import java.util.Optional;

public interface IStudentRepository
{

	StudentEntity save(StudentEntity student);

	StudentEntity update(StudentEntity student);

	void delete(Long id);

	Optional<StudentEntity> findById(Long id);

	Optional<StudentEntity> findByCode(String code);

	List<StudentEntity> findAll();

	List<StudentEntity> findByStatus(StudentStatus status);

	List<StudentEntity> findByGuardianId(Long guardianId);

	List<StudentEntity> findByAcademicYearId(Long academicYearId);

	List<StudentEntity> findByName(String name);

	long countAll();

	long countByGuardianId(Long guardianId);

	long countByAcademicYearId(Long academicYearId);

	long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	List<StudentEntity> findBySchoolId(Long schoolId);

	long countBySchoolId(Long schoolId);
}
