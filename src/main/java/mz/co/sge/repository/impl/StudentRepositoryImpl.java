package mz.co.sge.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.repository.IStudentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepositoryImpl implements IStudentRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public StudentEntity save(StudentEntity student)
	{
		if (student.getId() == null)
		{
			em.persist(student);
			return student;
		} else
		{
			return em.merge(student);
		}
	}

	@Override
	@Transactional
	public StudentEntity update(StudentEntity student)
	{
		return em.merge(student);
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		findById(id).ifPresent(em::remove);
	}

	@Override
	public Optional<StudentEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(StudentEntity.class, id));
	}

	@Override
	public Optional<StudentEntity> findByCode(String code)
	{
		List<StudentEntity> list = em.createNamedQuery("Student.findByCode", StudentEntity.class).setParameter("code", code).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public List<StudentEntity> findAll()
	{
		return em.createNamedQuery("Student.findAll", StudentEntity.class).getResultList();
	}

	@Override
	public List<StudentEntity> findByStatus(StudentStatus status)
	{
		return em.createNamedQuery("Student.findByStatus", StudentEntity.class).setParameter("status", status).getResultList();
	}

	@Override
	public List<StudentEntity> findByGuardianId(Long guardianId)
	{
		return em.createNamedQuery("Student.findByGuardian", StudentEntity.class).setParameter("guardianId", guardianId).getResultList();
	}

	@Override
	public List<StudentEntity> findByAcademicYearId(Long academicYearId)
	{
		return em.createNamedQuery("Student.findByAcademicYear", StudentEntity.class).setParameter("academicYearId", academicYearId).getResultList();
	}

	@Override
	public List<StudentEntity> findByName(String name)
	{
		if (name == null || name.isBlank())
			return findAll();
		return em.createQuery("SELECT s FROM StudentEntity s WHERE LOWER(s.firstName) LIKE :term OR LOWER(s.lastName) LIKE :term OR LOWER(s.code) LIKE :term",
				StudentEntity.class).setParameter("term", "%" + name.toLowerCase() + "%").getResultList();
	}

	@Override
	public long countAll()
	{
		return em.createNamedQuery("Student.countAll", Long.class).getSingleResult();
	}

	@Override
	public long countByGuardianId(Long guardianId)
	{
		return em.createNamedQuery("Student.countByGuardian", Long.class).setParameter("guardianId", guardianId).getSingleResult();
	}

	@Override
	public long countByAcademicYearId(Long academicYearId)
	{
		return em.createQuery("SELECT COUNT(s) FROM StudentEntity s WHERE s.academicYear.id = :academicYearId", Long.class)
				.setParameter("academicYearId", academicYearId).getSingleResult();
	}

	@Override
	public long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return em.createNamedQuery("Student.countBySchoolAndYear", Long.class).setParameter("schoolId", schoolId).setParameter("academicYearId", academicYearId)
				.getSingleResult();
	}

	@Override
	public List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return em.createNamedQuery("Student.findBySchoolAndYear", StudentEntity.class).setParameter("schoolId", schoolId)
				.setParameter("academicYearId", academicYearId).getResultList();
	}

	@Override
	public List<StudentEntity> findBySchoolId(Long schoolId)
	{
		return em.createQuery("SELECT s FROM StudentEntity s WHERE s.school.id = :schoolId ORDER BY s.firstName", StudentEntity.class)
				.setParameter("schoolId", schoolId).getResultList();
	}

	@Override
	public long countBySchoolId(Long schoolId)
	{
		return em.createQuery("SELECT COUNT(s) FROM StudentEntity s WHERE s.school.id = :schoolId", Long.class).setParameter("schoolId", schoolId)
				.getSingleResult();
	}
}
