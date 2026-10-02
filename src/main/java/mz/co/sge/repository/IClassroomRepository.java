package mz.co.sge.repository;

import java.util.List;

import mz.co.sge.entity.ClassroomEntity;

public interface IClassroomRepository
{
	ClassroomEntity save(ClassroomEntity entity);

	ClassroomEntity findById(Long id);

	List<ClassroomEntity> findAll();

	List<ClassroomEntity> findByAcademicYear(Long yearId);
}
