package mz.co.sge.repository.impl;

import java.util.List;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.repository.IClassroomRepository;

@Repository
public class ClassroomRepositoryImpl implements IClassroomRepository
{
	@PersistenceContext
	private EntityManager em;

	public ClassroomEntity save(ClassroomEntity entity)
	{
		if (entity.getId() == null)
		{
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	public ClassroomEntity findById(Long id)
	{
		return em.find(ClassroomEntity.class, id);
	}

	public List<ClassroomEntity> findAll()
	{
		try
		{
			return em.createNamedQuery("Classroom.findAll", ClassroomEntity.class).getResultList();
		} catch (Exception e)
		{
			// fallback se named query não existir
			return em.createQuery(
					"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear ORDER BY c.name",
					ClassroomEntity.class).getResultList();
		}
	}

	public List<ClassroomEntity> findByAcademicYear(Long yearId)
	{
		if (yearId == null)
		{
			return findAll();
		}
		try
		{
			return em.createNamedQuery("Classroom.findByAcademicYear", ClassroomEntity.class).setParameter("yearId", yearId).getResultList();
		} catch (Exception e)
		{
			e.printStackTrace();
			return em.createQuery(
					"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear WHERE c.academicYear.id = :yearId ORDER BY c.name",
					ClassroomEntity.class).setParameter("yearId", yearId).getResultList();
		}
	}

	public List<ClassroomEntity> search(String term, Long yearId)
	{
		if (yearId == null)
			yearId = 0L; // evita null
		String like = "%" + (term != null ? term.toLowerCase() : "") + "%";
		return em.createQuery(
				"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher WHERE c.academicYear.id = :yId AND (LOWER(c.name) LIKE :term OR LOWER(c.classLevel.name) LIKE :term) ORDER BY c.name",
				ClassroomEntity.class).setParameter("yId", yearId).setParameter("term", like).getResultList();
	}

	public long countByAcademicYear(Long yearId)
	{
		return em.createQuery("SELECT COUNT(c) FROM ClassroomEntity c WHERE c.academicYear.id = :yId", Long.class).setParameter("yId", yearId)
				.getSingleResult();
	}

	public void delete(Long id)
	{
		ClassroomEntity e = findById(id);
		if (e != null)
			em.remove(e);
	}
}