package mz.co.sge.service;

import java.util.List;
import mz.co.sge.entity.TeacherEntity;

public interface ITeacherService
{
	TeacherEntity save(TeacherEntity entity);

	TeacherEntity findById(Long id);

	List<TeacherEntity> findAll();

	List<TeacherEntity> findByAcademicYear(Long yearId);

	List<TeacherEntity> search(String term, Long yearId);

	void delete(Long id);
}