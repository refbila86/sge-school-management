package mz.co.sge.service;

import java.util.List;
import mz.co.sge.entity.ClassroomEntity;

public interface IClassroomService
{
	ClassroomEntity save(ClassroomEntity entity);

	ClassroomEntity findById(Long id);

	List<ClassroomEntity> findAll();

	List<ClassroomEntity> findByAcademicYear(Long yearId);

	List<ClassroomEntity> findBySchoolAndYear(Long schoolId, Long yearId);

	List<ClassroomEntity> search(String term);

	long countByAcademicYear(Long yearId);

	void delete(Long id);
}