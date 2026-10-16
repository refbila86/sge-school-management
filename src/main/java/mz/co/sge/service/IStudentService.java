package mz.co.sge.service;

import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import java.util.List;
import java.util.Optional;

public interface IStudentService
{

	// Leitura no padrão AcademicYear
	List<StudentEntity> getAllStudents();

	Optional<StudentEntity> getStudentById(Long id);

	Optional<StudentEntity> getStudentByCode(String code);

	List<StudentEntity> getStudentsByGuardianId(Long guardianId);

	List<StudentEntity> getStudentsByAcademicYearId(Long academicYearId);

	List<StudentEntity> getStudentsByStatus(StudentStatus status);

	List<StudentEntity> findByName(String name);

	// Escrita
	StudentEntity createStudent(StudentEntity student);

	StudentEntity updateStudent(StudentEntity student);

	void deleteStudent(Long id);

	StudentEntity registerStudent(StudentEntity student, Long guardianId);

	// Contagem
	long countStudents();

	long countByGuardianId(Long guardianId);

	List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	List<StudentEntity> findBySchoolId(Long schoolId);

	List<StudentEntity> getStudentsBySchoolId(Long schoolId);

	List<StudentEntity> getStudentsBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	long countBySchoolId(Long schoolId);

	long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	long countByAcademicYearId(Long academicYearId);
	
	
	StudentEntity getByIdAndSchoolAndYear(Long id, Long schoolId, Long yearId);

	// Compatibilidade com código antigo (default methods)
	default List<StudentEntity> findAll()
	{
		return getAllStudents();
	}

	default Optional<StudentEntity> findById(Long id)
	{
		return getStudentById(id);
	}

	default Optional<StudentEntity> findByCode(String code)
	{
		return getStudentByCode(code);
	}

	default List<StudentEntity> findByGuardianId(Long id)
	{
		return getStudentsByGuardianId(id);
	}

	default List<StudentEntity> findByAcademicYearId(Long id)
	{
		return getStudentsByAcademicYearId(id);
	}

	default List<StudentEntity> findByStatus(StudentStatus s)
	{
		return getStudentsByStatus(s);
	}

	default StudentEntity create(StudentEntity s)
	{
		return createStudent(s);
	}

	default StudentEntity update(StudentEntity s)
	{
		return updateStudent(s);
	}

	default void delete(Long id)
	{
		deleteStudent(id);
	}

	default long countAll()
	{
		return countStudents();
	}
}
