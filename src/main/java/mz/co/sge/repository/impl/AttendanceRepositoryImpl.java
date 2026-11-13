package mz.co.sge.repository.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.repository.IAttendanceRepository;

@Repository
public class AttendanceRepositoryImpl implements IAttendanceRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<AttendanceEntity> findByDateAndSubject(LocalDate date, Long subjectId, Long classroomId, Trimester trimester)
	{
		return em.createNamedQuery("AttendanceEntity.findByDateAndSubject", AttendanceEntity.class).setParameter("date", date)
				.setParameter("subjectId", subjectId).setParameter("classroomId", classroomId).setParameter("trimester", trimester).getResultList();
	}

	@Override
	public List<AttendanceEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester)
	{
		return em.createNamedQuery("AttendanceEntity.findByFilters", AttendanceEntity.class).setParameter("classroomId", classroomId)
				.setParameter("subjectId", subjectId).setParameter("yearId", yearId).setParameter("trimester", trimester).getResultList();
	}

	@Override
	public long countFaltasByStudentAndTrimester(Long studentId, Long subjectId, Trimester trimester)
	{
		return em.createNamedQuery("AttendanceEntity.countFaltasByStudentAndTrimester", Long.class).setParameter("studentId", studentId)
				.setParameter("subjectId", subjectId).setParameter("trimester", trimester).getSingleResult();
	}

	@Override
	public List<AttendanceEntity> findFaltasByStudentAndTrimester(Long studentId, Long subjectId, Trimester trimester)
	{
		return em.createNamedQuery("AttendanceEntity.findByStudentAndSubjectAndTrimester", AttendanceEntity.class).setParameter("studentId", studentId)
				.setParameter("subjectId", subjectId).setParameter("trimester", trimester).getResultList();
	}

	@Override
	public void save(AttendanceEntity entity)
	{
		if (entity.getId() == null)
			em.persist(entity);
		else
			em.merge(entity);
	}

	@Override
	public void saveAll(List<AttendanceEntity> entities)
	{
		for (AttendanceEntity e : entities)
			save(e);
	}
}