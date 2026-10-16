package mz.co.sge.repository.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.repository.IPaymentRepository;

@Repository
public class PaymentRepositoryImpl implements IPaymentRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public PaymentEntity save(PaymentEntity e)
	{
		em.persist(e);
		return e;
	}

	@Override
	public PaymentEntity update(PaymentEntity e)
	{
		return em.merge(e);
	}

	@Override
	public void delete(Long id)
	{
		PaymentEntity e = em.find(PaymentEntity.class, id);
		if (e != null)
			em.remove(e);
	}

	@Override
	public Optional<PaymentEntity> findById(Long id)
	{
		return Optional.ofNullable(em.find(PaymentEntity.class, id));
	}

	@Override
	public Optional<PaymentEntity> findWithItems(Long id)
	{
		List<PaymentEntity> list = em.createNamedQuery("Payment.findWithItems", PaymentEntity.class).setParameter("id", id).getResultList();
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Override
	public List<PaymentEntity> findBySchoolAndYearId(Long schoolId, Long yearId)
	{
		return em.createNamedQuery("Payment.findBySchoolAndYear", PaymentEntity.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId)
				.getResultList();
	}

	@Override
	public List<PaymentEntity> findByStudentId(Long studentId, Long yearId)
	{
		return em.createNamedQuery("Payment.findByStudent", PaymentEntity.class).setParameter("studentId", studentId).setParameter("yearId", yearId)
				.getResultList();
	}

	@Override
	public List<PaymentEntity> findByReceipt(String term, Long schoolId)
	{
		return em.createNamedQuery("Payment.findByReceipt", PaymentEntity.class).setParameter("term", term).setParameter("schoolId", schoolId).getResultList();
	}

	@Override
	public long countBySchoolAndYear(Long schoolId, Long yearId)
	{
		return em.createNamedQuery("Payment.countBySchoolAndYear", Long.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId)
				.getSingleResult();
	}

	@Override
	public BigDecimal sumTotalBySchoolAndYear(Long schoolId, Long yearId)
	{
		BigDecimal v = em.createNamedQuery("Payment.sumBySchoolAndYear", BigDecimal.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId)
				.getSingleResult();
		return v == null ? BigDecimal.ZERO : v;
	}
}