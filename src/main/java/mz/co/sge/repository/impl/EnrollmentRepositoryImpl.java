package mz.co.sge.repository.impl;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;
import mz.co.sge.repository.IEnrollmentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class EnrollmentRepositoryImpl implements IEnrollmentRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public EnrollmentEntity save(EnrollmentEntity enrollment)
	{
		em.persist(enrollment);
		return enrollment;
	}

	@Override
	@Transactional
	public EnrollmentEntity update(EnrollmentEntity enrollment)
	{
		return em.merge(enrollment);
	}

	@Override
	public Optional<EnrollmentEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(EnrollmentEntity.class, id));
	}

	@Override
	public List<EnrollmentEntity> findAll()
	{
		return em.createNamedQuery("EnrollmentEntity.findAll", EnrollmentEntity.class).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByAcademicYear(Long academicYearId)
	{
		return em.createNamedQuery("EnrollmentEntity.findByYear", EnrollmentEntity.class).setParameter("yearId", academicYearId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByStudent(Long studentId)
	{
		return em.createQuery("SELECT e FROM EnrollmentEntity e WHERE e.student.id = :sid ORDER BY e.academicYear.year DESC", EnrollmentEntity.class)
				.setParameter("sid", studentId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByStudentAndYear(Long studentId, Long yearId)
	{
		return em.createNamedQuery("EnrollmentEntity.findByStudentAndYear", EnrollmentEntity.class).setParameter("studentId", studentId)
				.setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByStatus(EnrollmentStatus status)
	{
		return em.createNamedQuery("EnrollmentEntity.findByStatus", EnrollmentEntity.class).setParameter("status", status).getResultList();
	}

	@Override
	public long countByAcademicYear(Long academicYearId)
	{
		return em.createNamedQuery("EnrollmentEntity.countByYear", Long.class).setParameter("yearId", academicYearId).getSingleResult();
	}

	@Override
	public long countByStudent(Long studentId)
	{
		return em.createNamedQuery("EnrollmentEntity.countByStudent", Long.class).setParameter("studentId", studentId).getSingleResult();
	}

	@Override
	public boolean existsEnrollment(Long studentId, Long academicYearId)
	{
		return em.createNamedQuery("EnrollmentEntity.existsByStudentAndYear", Boolean.class).setParameter("studentId", studentId)
				.setParameter("yearId", academicYearId).getSingleResult();
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		findById(id).ifPresent(em::remove);
	}

	@Override
	public List<StudentEntity> findStudentsWithPaidFees(Long schoolId, Long yearId, EnrollmentType type)
	{
		// Agora usa FeeType em vez de LIKE '%matric%'
		String feeCode = (type == EnrollmentType.FIRST_ENROLLMENT) ? "MATRICULA" : "RENOVACAO_MATRICULA";
		return em.createQuery("""
				SELECT DISTINCT s FROM StudentEntity s
				WHERE s.status = 'REGISTERED'
				AND EXISTS (
				    SELECT 1 FROM PaymentEntity p JOIN p.items pi
				    WHERE p.student.id = s.id
				    AND p.academicYear.id = :yearId
				    AND pi.feeItem.code = :feeCode
				    AND p.status = 'PAID'
				) ORDER BY s.firstName""", StudentEntity.class).setParameter("yearId", yearId).setParameter("feeCode", feeCode).getResultList();
	}

	@Override
	public List<StudentEntity> findAvailableForEnrollment( Long yearId)
	{
		return em.createQuery("""
				SELECT DISTINCT s FROM StudentEntity s
				WHERE s.status = 'REGISTERED'
				AND NOT EXISTS (
				    SELECT 1 FROM EnrollmentEntity e WHERE e.student.id = s.id AND e.academicYear.id = :yearId
				)
				AND EXISTS (
				    SELECT 1 FROM PaymentEntity p JOIN p.items pi
				    WHERE p.student.id = s.id AND p.academicYear.id = :yearId AND p.status = 'PAID'
				) ORDER BY s.firstName""", StudentEntity.class).setParameter("yearId", yearId).getResultList();
	}
}