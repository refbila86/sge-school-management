package mz.co.sge.repository.impl;

import java.util.List;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.TeacherEntity;
import mz.co.sge.repository.ITeacherRepository;

@Repository
public class TeacherRepositoryImpl implements ITeacherRepository
{
	@PersistenceContext
	private EntityManager em;

	@Override
	public TeacherEntity save(TeacherEntity entity)
	{
		if (entity.getId() == null)
		{
			if (entity.getCode() == null || entity.getCode().isEmpty())
			{
				Long count = em.createQuery("SELECT COUNT(t) FROM TeacherEntity t", Long.class).getSingleResult();
				entity.setCode(String.format("TEA-%04d", count + 1));
			}
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	@Override
	public TeacherEntity findById(Long id)
	{
		try
		{
			return em.createQuery(
					"SELECT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.user LEFT JOIN FETCH t.academicYear WHERE t.id = :id",
					TeacherEntity.class).setParameter("id", id).getSingleResult();
		} catch (Exception e)
		{
			return em.find(TeacherEntity.class, id);
		}
	}

	@Override
	public List<TeacherEntity> findAll()
	{
		try
		{
			return em.createQuery(
					"SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.user LEFT JOIN FETCH t.academicYear ORDER BY t.firstName",
					TeacherEntity.class).getResultList();
		} catch (Exception e)
		{
			return em.createQuery("SELECT t FROM TeacherEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.academicYear ORDER BY t.firstName",
					TeacherEntity.class).getResultList();
		}
	}

	@Override
	public List<TeacherEntity> findByAcademicYear(Long yearId)
	{
		if (yearId == null)
			return findAll();
		return em.createQuery(
				"SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.user LEFT JOIN FETCH t.academicYear WHERE t.academicYear.id = :yId ORDER BY t.firstName",
				TeacherEntity.class).setParameter("yId", yearId).getResultList();
	}

	@Override
	public List<TeacherEntity> findByUserId(Long userId)
	{
		return em.createQuery("SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects WHERE t.user.id = :userId", TeacherEntity.class)
				.setParameter("userId", userId).getResultList();
	}

	@Override
	public List<TeacherEntity> search(String term, Long yearId)
	{
		String like = "%" + (term != null ? term.toLowerCase() : "") + "%";
		if (yearId != null)
		{
			return em.createQuery(
					"SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects WHERE t.academicYear.id = :yId AND (LOWER(t.firstName) LIKE :term OR LOWER(t.lastName) LIKE :term OR LOWER(t.code) LIKE :term OR LOWER(t.email) LIKE :term) ORDER BY t.firstName",
					TeacherEntity.class).setParameter("yId", yearId).setParameter("term", like).getResultList();
		}
		return em.createQuery(
				"SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects WHERE LOWER(t.firstName) LIKE :term OR LOWER(t.lastName) LIKE :term OR LOWER(t.code) LIKE :term OR LOWER(t.email) LIKE :term ORDER BY t.firstName",
				TeacherEntity.class).setParameter("term", like).getResultList();
	}

	@Override
	public void delete(Long id)
	{
		TeacherEntity e = findById(id);
		if (e != null)
			em.remove(e);
	}
}