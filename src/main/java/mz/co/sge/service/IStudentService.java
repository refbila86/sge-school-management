package mz.co.sge.service;

import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import java.util.List;
import java.util.Optional;

public interface IStudentService
{

	// Cadastro único
	StudentEntity createStudent(StudentEntity student);

	StudentEntity registerStudent(StudentEntity student, Long guardianId);

	StudentEntity updateStudent(StudentEntity student);

	void deleteStudent(Long id);

	// Leitura
	List<StudentEntity> getAllStudents();

	Optional<StudentEntity> getStudentById(Long id);

	Optional<StudentEntity> getStudentByCode(String code);

	List<StudentEntity> getStudentsByGuardianId(Long guardianId);

	List<StudentEntity> getStudentsByStatus(StudentStatus status);

	List<StudentEntity> findByName(String name);

	// Via Matrícula - anos independentes
	List<StudentEntity> getStudentsBySchoolId(Long schoolId);

	List<StudentEntity> getStudentsBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	List<StudentEntity> getStudentsByAcademicYearId(Long academicYearId);

	StudentEntity getByIdAndSchoolAndYear(Long id, Long schoolId, Long yearId);
	
    List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    
    List<StudentEntity> findBySchoolId(Long schoolId);

	// Contagem
	long countStudents();

	long countBySchoolId(Long schoolId);

	long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

	long countByAcademicYearId(Long academicYearId);

	long countByGuardianId(Long guardianId);
}