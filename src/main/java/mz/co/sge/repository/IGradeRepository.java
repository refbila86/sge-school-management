package mz.co.sge.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import mz.co.sge.entity.GradeEntity;
import mz.co.sge.enumes.Trimester;

@Repository
public interface IGradeRepository
{
	List<GradeEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester);

	List<GradeEntity> findByStudentAndSubject(Long studentId, Long subjectId, Long yearId);

	void save(GradeEntity entity);

	void saveAll(List<GradeEntity> entities);

	void delete(GradeEntity entity);
}