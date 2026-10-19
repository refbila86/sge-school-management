package mz.co.sge.service.impl;

import mz.co.sge.entity.*;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;
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

	private EnrollmentEntity createInternal(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId)
	{
		if (enrollmentRepository.existsEnrollment(studentId, academicYearId))
		{
			throw new IllegalStateException("Aluno já possui matrícula neste ano letivo");
		}

		long totalPrevious = enrollmentRepository.countByStudent(studentId);
		EnrollmentType type = EnrollmentType.fromTotalPrevious(totalPrevious);

		AcademicYearEntity year = em.find(AcademicYearEntity.class, academicYearId);
		if (year == null)
			throw new jakarta.persistence.EntityNotFoundException("Ano letivo não encontrado: " + academicYearId);

		StudentEntity student = em.find(StudentEntity.class, studentId);
		if (student == null)
			throw new jakarta.persistence.EntityNotFoundException("Aluno não encontrado: " + studentId);

		if (student.getCode() == null || student.getCode().isBlank())
		{
			throw new IllegalStateException("Aluno " + student.getFullName() + " sem código gerado. Recrie o aluno.");
		}

		ClassLevelEntity classLevel = null;
		if (classLevelId != null)
		{
			classLevel = em.find(ClassLevelEntity.class, classLevelId);
		}

		UserEntity createdBy = null;
		if (createdByUserId != null)
		{
			createdBy = em.find(UserEntity.class, createdByUserId);
		}

		SchoolEntity school = null;
		try
		{
			school = em.createQuery("SELECT s FROM SchoolEntity s WHERE s.academicYear.id = :yearId", SchoolEntity.class).setParameter("yearId", academicYearId)
					.setMaxResults(1).getSingleResult();
		} catch (jakarta.persistence.NoResultException e)
		{

			try
			{
				school = em.createQuery("SELECT s FROM SchoolEntity s WHERE s.active = true ORDER BY s.id ASC", SchoolEntity.class).setMaxResults(1)
						.getSingleResult();
			} catch (Exception ex)
			{
				school = null;
			}
		}

		try
		{
			Long lastYearId = em
					.createQuery("SELECT e.academicYear.id FROM EnrollmentEntity e WHERE e.student.id = :sid ORDER BY e.enrollmentDate DESC", Long.class)
					.setParameter("sid", studentId).setMaxResults(1).getSingleResult();
		} catch (jakarta.persistence.NoResultException e)
		{
			// Primeira matrícula
		}

		EnrollmentEntity enrollment = new EnrollmentEntity();
		enrollment.setAcademicYear(year);
		enrollment.setStudent(student);
		enrollment.setClassLevel(classLevel);
		enrollment.setCreatedBy(createdBy);
		enrollment.setType(type);
		enrollment.setStatus(EnrollmentStatus.PENDING);
		enrollment.setEnrollmentDate(LocalDateTime.now());

		if (school != null)
		{
			try
			{
				enrollment.setSchool(school); // só se o campo ainda existir
			} catch (Exception ignore)
			{
			}
		}

		long nextEnrollmentSeq = totalPrevious + 1;
		String enrollmentCode = String.format("%s-%05d", student.getCode(), nextEnrollmentSeq);
		enrollment.setCode(enrollmentCode);

		return enrollmentRepository.save(enrollment);
	}

	@Override
	@Transactional
	public EnrollmentEntity createFirstEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId)
	{
		long total = enrollmentRepository.countByStudent(studentId);
		if (total > 0)
		{
			throw new IllegalStateException("Aluno já possui " + total + " matrícula(s). Use RENOVAÇÃO.");
		}
		return createInternal(academicYearId, studentId, classLevelId, createdByUserId);
	}

	@Override
	@Transactional
	public EnrollmentEntity createRenewal(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId)
	{
		long total = enrollmentRepository.countByStudent(studentId);
		if (total == 0)
		{
			throw new IllegalStateException("Aluno nunca foi matriculado. Use PRIMEIRA MATRÍCULA.");
		}
		return createInternal(academicYearId, studentId, classLevelId, createdByUserId);
	}

	@Override
	@Transactional
	public EnrollmentEntity createPreEnrollment(Long academicYearId, Long studentId, Long classLevelId, Long createdByUserId)
	{
		return createInternal(academicYearId, studentId, classLevelId, createdByUserId);
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
			throw new IllegalStateException("Fluxo: PENDING -> CONFIRMED -> ENROLLED. Atual: " + enrollment.getStatus());
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
	public List<EnrollmentEntity> findByStudent(Long studentId)
	{
		return enrollmentRepository.findByStudent(studentId);
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

	@Override
	public EnrollmentType getEnrollmentTypeForStudent(Long studentId)
	{
		return EnrollmentType.fromTotalPrevious(enrollmentRepository.countByStudent(studentId));
	}

	@Override
	public List<StudentEntity> findStudentsWithMatriculaPaid(Long yearId)
	{
		return enrollmentRepository.findAvailableForEnrollment(yearId);
	}

	@Override
	public List<StudentEntity> findAvailableStudentsForEnrollment(Long schoolId, Long yearId)
	{
		return enrollmentRepository.findAvailableForEnrollment(yearId);
	}

	@Override
	public List<ClassLevelEntity> findAllClassLevels()
	{
		return em.createQuery("SELECT c FROM ClassLevelEntity c ORDER BY c.name", ClassLevelEntity.class).getResultList();
	}

	@Override
	public Long getMatriculaFeeItemId(Long schoolId)
	{
		try
		{
			return em.createQuery("SELECT f.id FROM FeeItemEntity f WHERE f.code = 'MATRICULA' AND f.school.id = :sid", Long.class)
					.setParameter("sid", schoolId).setMaxResults(1).getSingleResult();
		} catch (Exception e)
		{
			return em.createQuery("SELECT f.id FROM FeeItemEntity f WHERE f.code = 'MATRICULA'", Long.class).setMaxResults(1).getSingleResult();
		}
	}

	@Override
	public List<EnrollmentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long yearId)
	{
		return em.createQuery("SELECT e FROM EnrollmentEntity e WHERE e.school.id = :schoolId AND e.academicYear.id = :yearId ORDER BY e.id DESC",
				EnrollmentEntity.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByAcademicYearId(Long yearId)
	{
		return em.createQuery("SELECT e FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId ORDER BY e.id DESC", EnrollmentEntity.class)
				.setParameter("yearId", yearId).getResultList();
	}

	public boolean existsByStudentIdAndYearId(Long studentId, Long yearId)
	{
		Long count = em.createQuery("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :sid AND e.academicYear.id = :yid", Long.class)
				.setParameter("sid", studentId).setParameter("yid", yearId).getSingleResult();
		return count > 0;
	}
}