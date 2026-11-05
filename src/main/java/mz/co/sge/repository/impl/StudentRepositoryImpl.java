package mz.co.sge.repository.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.repository.IStudentRepository;

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
		List<StudentEntity> list = em.createNamedQuery("StudentEntity.findByCode", StudentEntity.class).setParameter("code", code).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public Optional<StudentEntity> findByDocument(String doc)
	{
		List<StudentEntity> list = em.createNamedQuery("StudentEntity.findByDocument", StudentEntity.class).setParameter("doc", doc).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public List<StudentEntity> findAll()
	{
		return em.createNamedQuery("StudentEntity.findAll", StudentEntity.class).getResultList();
	}

	@Override
	public List<StudentEntity> findByStatus(StudentStatus status)
	{
		return em.createNamedQuery("StudentEntity.findByStatus", StudentEntity.class).setParameter("status", status).getResultList();
	}

	@Override
	public List<StudentEntity> findByGuardianId(Long guardianId)
	{
		return em.createNamedQuery("StudentEntity.findByGuardian", StudentEntity.class).setParameter("guardianId", guardianId).getResultList();
	}

	@Override
	public List<StudentEntity> findByAcademicYearId(Long academicYearId)
	{
		return em.createQuery("SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.academicYear.id = :yId ORDER BY s.firstName",
				StudentEntity.class).setParameter("yId", academicYearId).getResultList();
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
		return em.createNamedQuery("StudentEntity.countAll", Long.class).getSingleResult();
	}

	@Override
	public long countByGuardianId(Long guardianId)
	{
		return em.createNamedQuery("StudentEntity.countByGuardian", Long.class).setParameter("guardianId", guardianId).getSingleResult();
	}

	@Override
	public long countByAcademicYearId(Long academicYearId)
	{
		return em.createQuery("SELECT COUNT(DISTINCT s) FROM StudentEntity s JOIN s.enrollments e WHERE e.academicYear.id = :yId", Long.class)
				.setParameter("yId", academicYearId).getSingleResult();
	}

	@Override
	public long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return em.createNamedQuery("StudentEntity.countBySchoolAndYear", Long.class).setParameter("schoolId", schoolId)
				.setParameter("academicYearId", academicYearId).getSingleResult();
	}

	@Override
	public List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return em.createNamedQuery("StudentEntity.findBySchoolAndYear", StudentEntity.class).setParameter("schoolId", schoolId)
				.setParameter("academicYearId", academicYearId).getResultList();
	}

	@Override
	public List<StudentEntity> findBySchoolId(Long schoolId)
	{
		return em.createNamedQuery("StudentEntity.findBySchool", StudentEntity.class).setParameter("schoolId", schoolId).getResultList();
	}

	@Override
	public long countBySchoolId(Long schoolId)
	{
		return em.createQuery("SELECT COUNT(DISTINCT s) FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId", Long.class)
				.setParameter("schoolId", schoolId).getSingleResult();
	}

	@Override
	public List<StudentEntity> findAllWithDetails()
	{
		return em.createQuery("SELECT DISTINCT s FROM StudentEntity s LEFT JOIN FETCH s.guardian LEFT JOIN FETCH s.enrollments ORDER BY s.firstName",
				StudentEntity.class).getResultList();
	}

	@Override
	public Optional<StudentEntity> findByIdWithDetails(Long id)
	{
		List<StudentEntity> list = em
				.createQuery("SELECT DISTINCT s FROM StudentEntity s LEFT JOIN FETCH s.guardian LEFT JOIN FETCH s.enrollments WHERE s.id = :id",
						StudentEntity.class)
				.setParameter("id", id).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public Optional<StudentEntity> findByIdAndSchoolIdAndAcademicYearId(Long id, Long schoolId, Long yearId)
	{
		try
		{
			return Optional.of(em.createQuery(
					"SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE s.id = :id AND e.school.id = :schoolId AND e.academicYear.id = :yearId",
					StudentEntity.class).setParameter("id", id).setParameter("schoolId", schoolId).setParameter("yearId", yearId).getSingleResult());
		} catch (NoResultException e)
		{
			return Optional.empty();
		}
	}

	@Override
	public List<Long> findNewStudentIds()
	{
		return em.createNamedQuery("Student.findNewStudentIds", Long.class).getResultList();
	}

	@Override
	public List<Long> findIdsWithPaidPaymentInYear(List<Long> ids, Long yearId)
	{
		if (ids == null || ids.isEmpty())
			return List.of();
		return em.createNamedQuery("Student.findIdsWithPaidPaymentInYear", Long.class).setParameter("ids", ids).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<StudentEntity> findByIds(List<Long> ids)
	{
		if (ids == null || ids.isEmpty())
			return List.of();
		return em.createNamedQuery("Student.findByIds", StudentEntity.class).setParameter("ids", ids).getResultList();
	}

	@Override
	public List<StudentEntity> findByClassroom(Long classroomId, Long academicYearId)
	{
		return em.createNamedQuery("StudentEntity.findByClassroom", StudentEntity.class)
				.setParameter("classroomId", classroomId)
				.setParameter("yearId", academicYearId).getResultList();
	}

	@Override
	public List<StudentEntity> findByClassroomId(Long classroomId)
	{
		return em.createNamedQuery("StudentEntity.findByClassroomId", 
				StudentEntity.class).setParameter("classroomId", classroomId)
				.getResultList();
	}
}