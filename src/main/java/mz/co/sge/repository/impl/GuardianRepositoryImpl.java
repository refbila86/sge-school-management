package mz.co.sge.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.repository.IGuardianRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class GuardianRepositoryImpl implements IGuardianRepository
{
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public GuardianEntity save(GuardianEntity guardian)
	{
		if (guardian.getId() == null)
		{
			em.persist(guardian);
			return guardian;
		} else
		{
			return em.merge(guardian);
		}
	}

	@Override
	@Transactional
	public GuardianEntity update(GuardianEntity guardian)
	{
		return em.merge(guardian);
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		findById(id).ifPresent(em::remove);
	}

	@Override
	public Optional<GuardianEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(GuardianEntity.class, id));
	}

	@Override
	public Optional<GuardianEntity> findByCode(String code)
	{
		List<GuardianEntity> list = em.createQuery("SELECT g FROM GuardianEntity g WHERE g.code = :code", GuardianEntity.class).setParameter("code", code)
				.getResultList();
		return list.stream().findFirst();
	}

	@Override
	public Optional<GuardianEntity> findByUserId(Long userId)
	{
		List<GuardianEntity> list = em.createQuery("SELECT g FROM GuardianEntity g WHERE g.user.id = :userId", GuardianEntity.class)
				.setParameter("userId", userId).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public Optional<GuardianEntity> findWithStudents(Long id)
	{
		List<GuardianEntity> list = em.createQuery("SELECT DISTINCT g FROM GuardianEntity g LEFT JOIN FETCH g.students WHERE g.id = :id", GuardianEntity.class)
				.setParameter("id", id).getResultList();
		return list.stream().findFirst();
	}

	@Override
	public List<GuardianEntity> findAll()
	{
		return em.createQuery("SELECT g FROM GuardianEntity g ORDER BY g.firstName", GuardianEntity.class).getResultList();
	}

	@Override
	public List<GuardianEntity> findAllWithStudents()
	{
		return em.createQuery("SELECT DISTINCT g FROM GuardianEntity g LEFT JOIN FETCH g.students ORDER BY g.firstName", GuardianEntity.class).getResultList();
	}

	@Override
	public List<GuardianEntity> findByAcademicYearId(Long academicYearId)
	{
		return em.createQuery("SELECT g FROM GuardianEntity g WHERE g.academicYear.id = :academicYearId ORDER BY g.firstName", GuardianEntity.class)
				.setParameter("academicYearId", academicYearId).getResultList();
	}

	@Override
	public List<GuardianEntity> findBySchoolId(Long schoolId)
	{
		return em.createQuery(
				"SELECT DISTINCT g FROM GuardianEntity g LEFT JOIN FETCH g.students s WHERE s.school.id = :schoolId OR s.id IS NULL ORDER BY g.firstName",
				GuardianEntity.class).setParameter("schoolId", schoolId).getResultList();
	}

	@Override
	public List<GuardianEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return em.createQuery(
				"SELECT DISTINCT g FROM GuardianEntity g LEFT JOIN FETCH g.students s WHERE (s.school.id = :schoolId AND g.academicYear.id = :academicYearId) OR s.id IS NULL ORDER BY g.firstName",
				GuardianEntity.class).setParameter("schoolId", schoolId).setParameter("academicYearId", academicYearId).getResultList();
	}

	@Override
	public long countAll()
	{
		return em.createQuery("SELECT COUNT(g) FROM GuardianEntity g", Long.class).getSingleResult();
	}

	@Override
	public long countAllByAcademicYearId(Long academicYearId)
	{
		return em.createQuery("SELECT COUNT(g) FROM GuardianEntity g WHERE g.academicYear.id = :academicYearId", Long.class)
				.setParameter("academicYearId", academicYearId).getSingleResult();
	}
}