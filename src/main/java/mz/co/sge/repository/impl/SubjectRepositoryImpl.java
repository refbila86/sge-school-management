package mz.co.sge.repository.impl;

import jakarta.persistence.*;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.repository.ISubjectRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class SubjectRepositoryImpl implements ISubjectRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<SubjectEntity> findAll()
	{
		return em.createNamedQuery("SubjectEntity.findAll", SubjectEntity.class).getResultList();
	}

	@Override
	public List<SubjectEntity> findByAcademicYearId(Long yearId)
	{
		return em.createNamedQuery("SubjectEntity.findByAcademicYear", SubjectEntity.class).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public SubjectEntity findByCode(String code)
	{
		try
		{
			return em.createNamedQuery("SubjectEntity.findByCode", SubjectEntity.class).setParameter("code", code).getSingleResult();
		} catch (NoResultException e)
		{
			return null;
		}
	}

	@Override
	public List<SubjectEntity> findByName(String name)
	{
		return em.createNamedQuery("SubjectEntity.findByName", SubjectEntity.class).setParameter("name", "%" + name.toLowerCase() + "%").getResultList();
	}

	@Override
	public SubjectEntity findById(Long id)
	{
		return em.find(SubjectEntity.class, id);
	}

	@Override
	public SubjectEntity save(SubjectEntity entity)
	{
		if (entity.getId() == null)
		{
			em.persist(entity);
			return entity;
		} else
		{
			return em.merge(entity);
		}
	}

	@Override
	public void delete(Long id)
	{
		SubjectEntity s = findById(id);
		if (s != null)
			em.remove(s);
	}

	@Override
	public boolean existsByCode(String code)
	{
		return findByCode(code) != null;
	}

	@Override
	public List<SubjectEntity> findAllActive()
	{
		return em.createNamedQuery("SubjectEntity.findAllActive", SubjectEntity.class).getResultList();
	}
}