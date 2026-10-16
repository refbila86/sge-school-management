package mz.co.sge.repository;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import java.util.List;
import java.util.Optional;

public interface IEnrollmentRepository
{
	EnrollmentEntity save(EnrollmentEntity enrollment);

	EnrollmentEntity update(EnrollmentEntity enrollment);

	Optional<EnrollmentEntity> findById(Long id);

	List<EnrollmentEntity> findAll();

	List<EnrollmentEntity> findByAcademicYear(Long academicYearId);

	List<EnrollmentEntity> findByStudentAndYear(Long studentId, Long yearId);

	List<EnrollmentEntity> findByStatus(EnrollmentStatus status);

	long countByAcademicYear(Long academicYearId);

	void delete(Long id);

	boolean existsEnrollment(Long studentId, Long academicYearId);
}
