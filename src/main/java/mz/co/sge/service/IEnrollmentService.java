package mz.co.sge.service;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import java.util.List;

public interface IEnrollmentService
{
	EnrollmentEntity createPreEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId);

	EnrollmentEntity confirmPayment(Long enrollmentId, PaymentEntity payment);

	EnrollmentEntity enroll(Long enrollmentId, Long confirmedByUserId); // SÓ AQUI vira ENROLLED

	EnrollmentEntity cancel(Long enrollmentId, String reason);

	EnrollmentEntity findById(Long id);

	List<EnrollmentEntity> findAll();

	List<EnrollmentEntity> findByAcademicYear(Long yearId);

	List<EnrollmentEntity> findByStatus(EnrollmentStatus status);

	boolean canEnroll(Long studentId, Long academicYearId);
}
