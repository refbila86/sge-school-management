package mz.co.sge.repository.impl;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.repository.IMonthlyFeeRepository;

@Repository
public class MonthlyFeeRepositoryImpl implements IMonthlyFeeRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<MonthlyFeeEntity> findAll()
	{
		return em.createNamedQuery("MonthlyFeeEntity.findAll", MonthlyFeeEntity.class).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findByEnrollment", MonthlyFeeEntity.class).setParameter("enrollmentId", enrollmentId).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findByAcademicYearId(Long yearId)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findByAcademicYear", MonthlyFeeEntity.class).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findByStudentId(Long studentId)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findByStudent", MonthlyFeeEntity.class).setParameter("studentId", studentId).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findByYear(Integer year)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findByYear", MonthlyFeeEntity.class).setParameter("year", year).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findPaidByAcademicYearId(Long yearId)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findPaidByAcademicYear", MonthlyFeeEntity.class).setParameter("yearId", yearId).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findPaidByYear(Integer year)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findPaidByYear", MonthlyFeeEntity.class).setParameter("year", year).getResultList();
	}

	@Override
	public List<MonthlyFeeEntity> findUnpaidByStudentCode(String code)
	{
		return em.createNamedQuery("MonthlyFeeEntity.findUnpaidByStudentCode", MonthlyFeeEntity.class).setParameter("code", code).getResultList();
	}

	@Override
	public StudentEntity findStudentByCode(String code)
	{
		try
		{
			return em.createNamedQuery("MonthlyFeeEntity.findStudentByCode", StudentEntity.class).setParameter("code", code).getSingleResult();
		} catch (NoResultException e)
		{
			return null;
		}
	}

	@Override
	public MonthlyFeeEntity findByReference(String ref)
	{
		try
		{
			return em.createNamedQuery("MonthlyFeeEntity.findByReference", MonthlyFeeEntity.class).setParameter("ref", ref).getSingleResult();
		} catch (NoResultException e)
		{
			return null;
		}
	}

	@Override
	public boolean existsByReference(String ref)
	{
		Long c = em.createNamedQuery("MonthlyFeeEntity.existsByReference", Long.class).setParameter("ref", ref).getSingleResult();
		return c > 0;
	}

	@Override
	public boolean existsByEnrollmentMonthYear(Long eid, Integer month, Integer year)
	{
		Long c = em.createNamedQuery("MonthlyFeeEntity.existsByEnrollmentMonthYear", Long.class).setParameter("eid", eid).setParameter("m", month)
				.setParameter("y", year).getSingleResult();
		return c > 0;
	}

	@Override
	public MonthlyFeeEntity save(MonthlyFeeEntity e)
	{
		if (e.getId() == null)
		{
			em.persist(e);
			return e;
		} else
			return em.merge(e);
	}

	@Override
	public MonthlyFeeEntity payFee(Long id)
	{
		MonthlyFeeEntity f = em.find(MonthlyFeeEntity.class, id);
		return f;
	}
}