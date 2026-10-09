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
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	@Override
	public TeacherEntity findById(Long id)
	{
		return em.find(TeacherEntity.class, id);
	}

	@Override
	public List<TeacherEntity> findAll()
	{
		try
		{
			return em.createNamedQuery("Teacher.findAll", TeacherEntity.class).getResultList();
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
				"SELECT t FROM TeacherEntity t LEFT JOIN FETCH t.user LEFT JOIN FETCH t.academicYear WHERE t.academicYear.id = :yId ORDER BY t.firstName",
				TeacherEntity.class).setParameter("yId", yearId).getResultList();
	}

	@Override
	public List<TeacherEntity> findByUserId(Long userId)
	{
		return em.createNamedQuery("Teacher.findByUserId", TeacherEntity.class).setParameter("userId", userId).getResultList();
	}

	@Override
	public List<TeacherEntity> search(String term, Long yearId)
	{
		String like = "%" + (term != null ? term.toLowerCase() : "") + "%";
		if (yearId != null)
		{
			return em.createQuery(
					"SELECT t FROM TeacherEntity t WHERE t.academicYear.id = :yId AND (LOWER(t.firstName) LIKE :term OR LOWER(t.lastName) LIKE :term OR LOWER(t.code) LIKE :term OR LOWER(t.email) LIKE :term) ORDER BY t.firstName",
					TeacherEntity.class).setParameter("yId", yearId).setParameter("term", like).getResultList();
		}
		return em.createQuery(
				"SELECT t FROM TeacherEntity t WHERE LOWER(t.firstName) LIKE :term OR LOWER(t.lastName) LIKE :term OR LOWER(t.code) LIKE :term OR LOWER(t.email) LIKE :term ORDER BY t.firstName",
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