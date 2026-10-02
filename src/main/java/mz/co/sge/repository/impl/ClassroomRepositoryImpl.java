
package mz.co.sge.repository.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.repository.IClassroomRepository;

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
		return em.createNamedQuery("Classroom.findAll", ClassroomEntity.class).getResultList();
	}

	public List<ClassroomEntity> findByAcademicYear(Long yearId)
	{
		return em.createNamedQuery("Classroom.findByAcademicYear", ClassroomEntity.class).setParameter("yearId", yearId).getResultList();
	}
}