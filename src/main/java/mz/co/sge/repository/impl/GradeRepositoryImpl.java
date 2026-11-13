package mz.co.sge.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.GradeEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.repository.IGradeRepository;

@Repository
public class GradeRepositoryImpl implements IGradeRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<GradeEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester)
	{
		return em.createNamedQuery("GradeEntity.findByFilters", GradeEntity.class).setParameter("classroomId", classroomId).setParameter("subjectId", subjectId)
				.setParameter("yearId", yearId).setParameter("trimester", trimester).getResultList();
	}

	@Override
	public List<GradeEntity> findByStudentAndSubject(Long studentId, Long subjectId, Long yearId)
	{
		return em.createNamedQuery("GradeEntity.findByStudentAndSubject", GradeEntity.class).setParameter("studentId", studentId)
				.setParameter("subjectId", subjectId).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public void save(GradeEntity entity)
	{
		if (entity.getId() == null)
			em.persist(entity);
		else
			em.merge(entity);
	}

	@Override
	public void saveAll(List<GradeEntity> entities)
	{
		for (GradeEntity e : entities)
			save(e);
	}

	@Override
	public void delete(GradeEntity entity)
	{
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}
}