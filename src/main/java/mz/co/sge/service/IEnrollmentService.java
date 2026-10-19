package mz.co.sge.service;

import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;
import java.util.List;

public interface IEnrollmentService
{

	EnrollmentEntity createFirstEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId);

	EnrollmentEntity createRenewal(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId);

	// Mantido por compatibilidade - agora detecta automaticamente o tipo
	EnrollmentEntity createPreEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId);

	EnrollmentEntity confirmPayment(Long enrollmentId, PaymentEntity payment);

	EnrollmentEntity enroll(Long enrollmentId, Long confirmedByUserId); // SÓ AQUI vira ENROLLED

	EnrollmentEntity cancel(Long enrollmentId, String reason);

	// --- CONSULTAS ---
	EnrollmentEntity findById(Long id);

	List<EnrollmentEntity> findAll();

	List<EnrollmentEntity> findByAcademicYear(Long yearId);

	List<EnrollmentEntity> findByStudent(Long studentId);

	List<EnrollmentEntity> findByStatus(EnrollmentStatus status);

	boolean canEnroll(Long studentId, Long academicYearId);

	EnrollmentType getEnrollmentTypeForStudent(Long studentId);

	// --- ALUNOS APTOS (REGRA NOVA: só quem pagou a taxa certa) ---
	List<StudentEntity> findStudentsWithMatriculaPaid(Long yearId);

	List<StudentEntity> findAvailableStudentsForEnrollment(Long schoolId, Long yearId);

	// --- AUXILIARES ---
	List<ClassLevelEntity> findAllClassLevels();

	Long getMatriculaFeeItemId(Long schoolId);
	
	List<EnrollmentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long yearId);
	
	List<EnrollmentEntity> findByAcademicYearId(Long yearId);
	
	boolean existsByStudentIdAndYearId(Long studentId, Long yearId);
}