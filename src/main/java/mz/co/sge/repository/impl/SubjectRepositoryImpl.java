//package mz.co.sge.repository.impl;
//
//import java.util.List;
//
//import org.springframework.stereotype.Repository;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import mz.co.sge.entity.SubjectEntity;
//import mz.co.sge.repository.ISubjectRepository;
//
//@Repository // <--- COLOCA ISSO
//public class SubjectRepositoryImpl implements ISubjectRepository
//{
//	@PersistenceContext
//	private EntityManager em;
//
//	public SubjectEntity save(SubjectEntity entity)
//	{
//		if (entity.getId() == null)
//		{
//			em.persist(entity);
//			return entity;
//		}
//		return em.merge(entity);
//	}
//
//	public SubjectEntity findById(Long id)
//	{
//		return em.find(SubjectEntity.class, id);
//	}
//
//	public List<SubjectEntity> findAll()
//	{
//		return em.createNamedQuery("Subject.findAll", SubjectEntity.class).getResultList();
//	}
//
//	public List<SubjectEntity> findByAcademicYear(Long yearId)
//	{
//		return em.createNamedQuery("Subject.findByAcademicYear", SubjectEntity.class).setParameter("yearId", yearId).getResultList();
//	}
//
//	public SubjectEntity findByCode(String code)
//	{
//		return em.createNamedQuery("Subject.findByCode", SubjectEntity.class).setParameter("code", code).getResultStream().findFirst().orElse(null);
//	}
//
//}
