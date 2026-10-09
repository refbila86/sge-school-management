package mz.co.sge.repository;

import java.util.List;
import mz.co.sge.entity.TeacherEntity;

public interface ITeacherRepository
{
	TeacherEntity save(TeacherEntity entity);

	TeacherEntity findById(Long id);

	List<TeacherEntity> findAll();

	List<TeacherEntity> findByAcademicYear(Long yearId);

	List<TeacherEntity> findByUserId(Long userId);

	List<TeacherEntity> search(String term, Long yearId);

	void delete(Long id);
}