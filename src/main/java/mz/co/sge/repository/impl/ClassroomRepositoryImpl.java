package mz.co.sge.repository.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.repository.IClassroomRepository;

@Repository
public class ClassroomRepositoryImpl implements IClassroomRepository
{
	@PersistenceContext
	private EntityManager em;

	public ClassroomEntity save(ClassroomEntity e)
	{
		if (e.getId() == null)
		{
			em.persist(e);
			return e;
		}
		return em.merge(e);
	}

	public ClassroomEntity findById(Long id)
	{
		try
		{
			return em.find(ClassroomEntity.class, id);
		} catch (Exception ex)
		{
			return null;
		}
	}

	public List<ClassroomEntity> findAll()
	{
		try
		{
			return em.createNamedQuery("Classroom.findAll", ClassroomEntity.class).getResultList();
		} catch (Exception e1)
		{
			try
			{
				return em.createQuery(
						"SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher ORDER BY c.name",
						ClassroomEntity.class).getResultList();
			} catch (Exception e2)
			{
				e2.printStackTrace();
				return new ArrayList<>();
			}
		}
	}

	public List<ClassroomEntity> findByAcademicYear(Long yId)
	{
		if (yId == null)
			return findAll();
		try
		{
			return em.createNamedQuery("Classroom.findByAcademicYear", ClassroomEntity.class).setParameter("yearId", yId).getResultList();
		} catch (Exception e1)
		{
			try
			{
				return em.createQuery(
						"SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher WHERE c.academicYear.id=:yearId ORDER BY c.name",
						ClassroomEntity.class).setParameter("yearId", yId).getResultList();
			} catch (Exception e2)
			{
				return findAll();
			}
		}
	}

	public List<ClassroomEntity> search(String term, Long yId)
	{
		try
		{
			if (term == null || term.isBlank())
				return yId != null ? findByAcademicYear(yId) : findAll();
			String like = "%" + term.toLowerCase() + "%";
			if (yId == null || yId == 0)
				return em.createQuery(
						"SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher WHERE LOWER(c.name) LIKE :t OR LOWER(c.classLevel.name) LIKE :t ORDER BY c.name",
						ClassroomEntity.class).setParameter("t", like).getResultList();
			return em.createQuery(
					"SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher WHERE c.academicYear.id=:yId AND (LOWER(c.name) LIKE :t OR LOWER(c.classLevel.name) LIKE :t) ORDER BY c.name",
					ClassroomEntity.class).setParameter("yId", yId).setParameter("t", like).getResultList();
		} catch (Exception e)
		{
			return new ArrayList<>();
		}
	}

	public long countByAcademicYear(Long yId)
	{
		try
		{
			return em.createQuery("SELECT COUNT(c) FROM ClassroomEntity c WHERE c.academicYear.id=:yId", Long.class).setParameter("yId", yId).getSingleResult();
		} catch (Exception e)
		{
			return 0;
		}
	}

	public void delete(Long id)
	{
		try
		{
			ClassroomEntity e = findById(id);
			if (e != null)
				em.remove(e);
		} catch (Exception ex)
		{
		}
	}

	public void incrementStudents(Long id)
	{
		try
		{
			em.createQuery("UPDATE ClassroomEntity c SET c.currentStudents=c.currentStudents+1 WHERE c.id=:id").setParameter("id", id).executeUpdate();
		} catch (Exception e)
		{
		}
	}

	public void decrementStudents(Long id)
	{
		try
		{
			em.createQuery("UPDATE ClassroomEntity c SET c.currentStudents=CASE WHEN c.currentStudents>0 THEN c.currentStudents-1 ELSE 0 END WHERE c.id=:id")
					.setParameter("id", id).executeUpdate();
		} catch (Exception e)
		{
		}
	}

	public List<ClassroomEntity> findAllActive()
	{
		try
		{
			return em.createNamedQuery("ClassroomEntity.findAllActive", ClassroomEntity.class).getResultList();
		} catch (Exception e)
		{
			return findAll();
		}
	}

	@Override
	public List<ClassroomEntity> findByAcademicYearAndClassLevel(Long yearId, Long classLevelId) {
	    try {
	        return em.createNamedQuery("ClassroomEntity.findByAcademicYearAndClassLevel", ClassroomEntity.class)
	                 .setParameter("yearId", yearId)
	                 .setParameter("classLevelId", classLevelId)
	                 .getResultList();
	    } catch (Exception e) {
	        return new ArrayList<>();
	    }
	}
}