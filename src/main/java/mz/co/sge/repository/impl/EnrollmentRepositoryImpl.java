package mz.co.sge.repository.impl;

import mz.co.sge.entity.EnrollmentEntity;
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
}
