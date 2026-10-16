package mz.co.sge.repository.impl;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
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
		return em.createNamedQuery("Enrollment.findAll", EnrollmentEntity.class).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByAcademicYear(Long academicYearId)
	{
		return em.createNamedQuery("Enrollment.findByYear", EnrollmentEntity.class).setParameter("yearId", academicYearId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByStudentAndYear(Long studentId, Long yearId)
	{
		return em.createNamedQuery("Enrollment.findByStudentAndYear", EnrollmentEntity.class).setParameter("studentId", studentId)
				.setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<EnrollmentEntity> findByStatus(EnrollmentStatus status)
	{
		return em.createNamedQuery("Enrollment.findByStatus", EnrollmentEntity.class).setParameter("status", status).getResultList();
	}

	@Override
	public long countByAcademicYear(Long academicYearId)
	{
		return (Long) em.createNamedQuery("Enrollment.countByYear").setParameter("yearId", academicYearId).getSingleResult();
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		findById(id).ifPresent(em::remove);
	}

	@Override
	public boolean existsEnrollment(Long studentId, Long academicYearId)
	{
		Long count = em.createQuery("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :sid AND e.academicYear.id = :yid", Long.class)
				.setParameter("sid", studentId).setParameter("yid", academicYearId).getSingleResult();
		return count > 0;
	}

	@Override
	public Long findMatriculaFeeItemId(Long schoolId)
	{
		try
		{
			return em.createQuery("SELECT f.id FROM FeeItemEntity f WHERE f.school.id = :schoolId AND LOWER(f.descricao) LIKE '%matric%' ORDER BY f.id ASC",
					Long.class).setParameter("schoolId", schoolId).setMaxResults(1).getSingleResult();
		} catch (Exception e)
		{
			// fallback: busca sem school
			try
			{
				return em.createQuery("SELECT f.id FROM FeeItemEntity f WHERE LOWER(f.descricao) LIKE '%matric%' ORDER BY f.id ASC", Long.class)
						.setMaxResults(1).getSingleResult();
			} catch (Exception ex)
			{
				return null;
			}
		}
	}

	@Override
	public List<StudentEntity> findStudentsWithMatriculaPaid(Long schoolId, Long matriculaFeeItemId)
	{
		if (matriculaFeeItemId == null)
			return List.of();
		return em
				.createQuery("SELECT DISTINCT s FROM StudentEntity s WHERE s.school.id = :schoolId AND s.status = 'REGISTERED' "
						+ "AND EXISTS (SELECT 1 FROM PaymentEntity p JOIN p.items pi WHERE p.student.id = s.id AND pi.feeItem.id = :matId) "
						+ "ORDER BY s.firstName", StudentEntity.class)
				.setParameter("schoolId", schoolId).setParameter("matId", matriculaFeeItemId).getResultList();
	}

	@Override
	public List<StudentEntity> findStudentsWithMatriculaPaidBySchoolAndYear(Long schoolId, Long yearId)
	{
		Long matId = findMatriculaFeeItemId(schoolId);
		if (matId == null)
		{
			// busca sem schoolId se não achar
			try
			{
				matId = em.createQuery("SELECT f.id FROM FeeItemEntity f WHERE LOWER(f.descricao) LIKE '%matric%' ORDER BY f.id", Long.class).setMaxResults(1)
						.getSingleResult();
			} catch (Exception e)
			{
				return List.of();
			}
		}

		// IMPORTANTE: filtra pagamento por academic_year_id do login
		return em
				.createQuery(
						"SELECT DISTINCT s FROM StudentEntity s " + "WHERE s.school.id = :schoolId AND s.status = 'REGISTERED' " + "AND EXISTS ("
								+ "  SELECT 1 FROM PaymentEntity p JOIN p.items pi " + "  WHERE p.student.id = s.id " + "  AND pi.feeItem.id = :matId "
								+ "  AND (p.academicYear.id = :yearId OR pi.feeItem.academicYear.id = :yearId) " + ") ORDER BY s.firstName",
						StudentEntity.class)
				.setParameter("schoolId", schoolId).setParameter("yearId", yearId).setParameter("matId", matId).getResultList();
	}

	@Override
	public List<StudentEntity> findAvailableStudentsForEnrollment(Long schoolId, Long yearId)
	{
		Long matId = findMatriculaFeeItemId(schoolId);
		if (matId == null)
			return List.of();

		return em
				.createQuery("SELECT DISTINCT s FROM StudentEntity s " + "WHERE s.school.id = :schoolId AND s.status = 'REGISTERED' " + "AND EXISTS ("
						+ "  SELECT 1 FROM PaymentEntity p JOIN p.items pi " + "  WHERE p.student.id = s.id " + "  AND pi.feeItem.id = :matId "
						+ "  AND (p.academicYear.id = :yearId OR pi.feeItem.academicYear.id = :yearId) " + ") "
						+ "AND NOT EXISTS (SELECT 1 FROM EnrollmentEntity e WHERE e.student.id = s.id AND e.academicYear.id = :yearId) "
						+ "ORDER BY s.firstName", StudentEntity.class)
				.setParameter("schoolId", schoolId).setParameter("yearId", yearId).setParameter("matId", matId).getResultList();
	}

}
