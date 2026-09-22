package mz.co.sge.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.repository.IAcademicYearRepository;

@Repository
@Transactional(readOnly = true)
public class AcademicYearRepositoryImpl implements IAcademicYearRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<AcademicYearEntity> findAll()
	{
		return em.createNamedQuery("AcademicYearEntity.findAll", AcademicYearEntity.class).getResultList();
	}

	@Override
	public Optional<AcademicYearEntity> findActive()
	{
		try
		{
			return Optional.of(em.createNamedQuery("AcademicYearEntity.findActive", AcademicYearEntity.class).setMaxResults(1).getSingleResult());
		} catch (Exception e)
		{
			return Optional.empty();
		}
	}

	@Override
	public Optional<AcademicYearEntity> findByDescription(String year)
	{
		try
		{
			return Optional
					.of(em.createNamedQuery("AcademicYearEntity.findByDescription", AcademicYearEntity.class).setParameter("year", year).getSingleResult());
		} catch (Exception e)
		{
			return Optional.empty();
		}
	}

	@Override
	public Optional<AcademicYearEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(AcademicYearEntity.class, id));
	}

	@Override
	public Optional<AcademicYearEntity> findByYear(String year)
	{
		// sua query agora usa :description mas busca por year
		return findByDescription(year);
	}

	@Override
	@Transactional
	public AcademicYearEntity save(AcademicYearEntity entity)
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
	@Transactional
	public void delete(Long id)
	{
		findById(id).ifPresent(em::remove);
	}

	@Override
	public Long count()
	{
		return em.createQuery("SELECT COUNT(a) FROM AcademicYearEntity a", Long.class).getSingleResult();
	}

	@Override
	public List<AcademicYearEntity> findByYearContaining(String year)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AcademicYearEntity> findByYear(int year)
	{
		// TODO Auto-generated method stub
		return null;
	}
}