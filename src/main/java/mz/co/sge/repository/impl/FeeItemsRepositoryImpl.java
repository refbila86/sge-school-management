package mz.co.sge.repository.impl;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import mz.co.sge.entity.FeeItemEntity;
import mz.co.sge.repository.IFeeItemsRepository;

@Repository
public class FeeItemsRepositoryImpl implements IFeeItemsRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public FeeItemEntity save(FeeItemEntity entity)
	{
		em.persist(entity);
		return entity;
	}

	@Override
	public FeeItemEntity update(FeeItemEntity entity)
	{
		return em.merge(entity);
	}

	@Override
	public void delete(Long id)
	{
		FeeItemEntity e = em.find(FeeItemEntity.class, id);
		if (e != null)
			em.remove(e);
	}

	@Override
	public Optional<FeeItemEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(FeeItemEntity.class, id));
	}

	@Override
	public List<FeeItemEntity> findAll()
	{
		return em.createNamedQuery("FeeItem.findAll", FeeItemEntity.class).getResultList();
	}

	@Override
	public List<FeeItemEntity> findBySchoolId(Long schoolId)
	{
		return em.createNamedQuery("FeeItem.findBySchool", FeeItemEntity.class).setParameter("schoolId", schoolId).getResultList();
	}

	@Override
	public List<FeeItemEntity> findBySchoolAndYearId(Long schoolId, Long yearId)
	{
		return em.createNamedQuery("FeeItem.findBySchoolAndYear", FeeItemEntity.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId)
				.getResultList();
	}

	@Override
	public List<FeeItemEntity> findActiveBySchoolAndYearId(Long schoolId, Long yearId)
	{
		return em.createNamedQuery("FeeItem.findActiveBySchool", FeeItemEntity.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId)
				.getResultList();
	}

	@Override
	public List<FeeItemEntity> findByDescricao(String term, Long schoolId, Long yearId)
	{
		return em.createNamedQuery("FeeItem.findByDescricao", FeeItemEntity.class).setParameter("term", term).setParameter("schoolId", schoolId)
				.setParameter("yearId", yearId).getResultList();
	}

	@Override
	public long countBySchoolId(Long schoolId)
	{
		return em.createNamedQuery("FeeItem.countBySchool", Long.class).setParameter("schoolId", schoolId).getSingleResult();
	}
}