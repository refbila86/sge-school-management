package mz.co.sge.service.impl;

import mz.co.sge.entity.*;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.repository.IEnrollmentRepository;
import mz.co.sge.service.IEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements IEnrollmentService
{

	@Autowired
	private IEnrollmentRepository enrollmentRepository;

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public EnrollmentEntity createPreEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId)
	{
		if (enrollmentRepository.existsEnrollment(studentId, academicYearId))
		{
			throw new IllegalStateException("Aluno já possui matrícula neste ano letivo");
		}
		AcademicYearEntity year = em.find(AcademicYearEntity.class, academicYearId);
		StudentEntity student = em.find(StudentEntity.class, studentId);
		ClassLevelEntity classLevel = em.find(ClassLevelEntity.class, classLevelId);
		UserEntity createdBy = null;
		if (createdByUserId != null)
		{
			createdBy = em.find(UserEntity.class, createdByUserId);
		}

		EnrollmentEntity enrollment = new EnrollmentEntity();
		enrollment.setAcademicYear(year);
		enrollment.setStudent(student);
		enrollment.setClassLevel(classLevel);
		enrollment.setCreatedBy(createdBy);
		enrollment.setStatus(EnrollmentStatus.PENDING);
		enrollment.setEnrollmentDate(LocalDateTime.now());

		return enrollmentRepository.save(enrollment);
	}

	@Override
	@Transactional
	public EnrollmentEntity confirmPayment(Long enrollmentId, PaymentEntity payment)
	{
		EnrollmentEntity enrollment = findById(enrollmentId);
		if (enrollment.getStatus() != EnrollmentStatus.PENDING)
		{
			throw new IllegalStateException("Só PENDING pode confirmar. Atual: " + enrollment.getStatus());
		}
		if (payment.getId() == null)
		{
			em.persist(payment);
		} else
		{
			payment = em.merge(payment);
		}
		enrollment.setPayment(payment);
		enrollment.setStatus(EnrollmentStatus.CONFIRMED);
		return enrollmentRepository.update(enrollment);
	}

	@Override
	@Transactional
	public EnrollmentEntity enroll(Long enrollmentId, Long confirmedByUserId)
	{
		EnrollmentEntity enrollment = findById(enrollmentId);
		if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED)
		{
			throw new IllegalStateException("Fluxo obrigatório: PENDING -> CONFIRMED -> ENROLLED. Atual: " + enrollment.getStatus());
		}
		if (enrollment.getPayment() == null)
		{
			throw new IllegalStateException("Sem pagamento");
		}
		enrollment.setStatus(EnrollmentStatus.ENROLLED);
		enrollment.setEnrollmentDate(LocalDateTime.now());
		return enrollmentRepository.update(enrollment);
	}

	@Override
	@Transactional
	public EnrollmentEntity cancel(Long enrollmentId, String reason)
	{
		EnrollmentEntity enrollment = findById(enrollmentId);
		if (enrollment.getStatus() == EnrollmentStatus.ENROLLED)
		{
			throw new IllegalStateException("ENROLLED não pode cancelar");
		}
		enrollment.setStatus(EnrollmentStatus.CANCELLED);
		return enrollmentRepository.update(enrollment);
	}

	@Override
	public EnrollmentEntity findById(Long id)
	{
		return enrollmentRepository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Matrícula não encontrada: " + id));
	}

	@Override
	public List<EnrollmentEntity> findAll()
	{
		return enrollmentRepository.findAll();
	}

	@Override
	public List<EnrollmentEntity> findByAcademicYear(Long yearId)
	{
		return enrollmentRepository.findByAcademicYear(yearId);
	}

	@Override
	public List<EnrollmentEntity> findByStatus(EnrollmentStatus status)
	{
		return enrollmentRepository.findByStatus(status);
	}

	@Override
	public boolean canEnroll(Long studentId, Long academicYearId)
	{
		return !enrollmentRepository.existsEnrollment(studentId, academicYearId);
	}

	// NOVOS MÉTODOS - fee_item_id MATRICULA
	@Override
	public List<StudentEntity> findStudentsWithMatriculaPaid(Long schoolId, Long yearId)
	{
		return enrollmentRepository.findStudentsWithMatriculaPaidBySchoolAndYear(schoolId, yearId);
	}

	@Override
	public List<StudentEntity> findAvailableStudentsForEnrollment(Long schoolId, Long yearId)
	{
		return enrollmentRepository.findAvailableStudentsForEnrollment(schoolId, yearId);
	}

	@Override
	public List<ClassLevelEntity> findAllClassLevels()
	{
		return em.createQuery("SELECT c FROM ClassLevelEntity c ORDER BY c.name", ClassLevelEntity.class).getResultList();
	}

	@Override
	public Long getMatriculaFeeItemId(Long schoolId)
	{
		return enrollmentRepository.findMatriculaFeeItemId(schoolId);
	}
}
