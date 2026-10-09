package mz.co.sge.repository.impl;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.repository.IClassLevelRepository;

@Repository
public class ClassLevelRepositoryImpl implements IClassLevelRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public ClassLevelEntity save(ClassLevelEntity entity)
	{
		if (entity.getId() == null)
		{
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	@Override
	public ClassLevelEntity findById(Long id)
	{
		return em.find(ClassLevelEntity.class, id);
	}

	@Override
	public List<ClassLevelEntity> findAll()
	{
		return em.createNamedQuery("ClassLevel.findAll", ClassLevelEntity.class).getResultList();
	}

	@Override
	public List<ClassLevelEntity> findByAcademicYear(Long yearId)
	{
		return em.createNamedQuery("ClassLevel.findByAcademicYear", ClassLevelEntity.class).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public void delete(Long id)
	{
		ClassLevelEntity e = findById(id);
		if (e != null)
			em.remove(e);
	}
}