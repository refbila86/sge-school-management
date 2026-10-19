package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;

public interface IEnrollmentRepository
{

	EnrollmentEntity save(EnrollmentEntity enrollment);

	EnrollmentEntity update(EnrollmentEntity enrollment);

	Optional<EnrollmentEntity> findById(Long id);

	List<EnrollmentEntity> findAll();

	List<EnrollmentEntity> findByAcademicYear(Long academicYearId);

	List<EnrollmentEntity> findByStudent(Long studentId);

	List<EnrollmentEntity> findByStudentAndYear(Long studentId, Long yearId);

	List<EnrollmentEntity> findByStatus(EnrollmentStatus status);

	long countByAcademicYear(Long academicYearId);

	long countByStudent(Long studentId); 

	boolean existsEnrollment(Long studentId, Long academicYearId);

	void delete(Long id);

	List<StudentEntity> findStudentsWithPaidFees(Long schoolId, Long yearId, EnrollmentType type);

	List<StudentEntity> findAvailableForEnrollment(Long yearId);
}