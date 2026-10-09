package mz.co.sge.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import mz.co.sge.entity.ClassroomEntity;

@Repository
public interface IClassroomRepository
{
	ClassroomEntity save(ClassroomEntity entity);

	ClassroomEntity findById(Long id);

	List<ClassroomEntity> findAll();

	List<ClassroomEntity> findByAcademicYear(Long yearId);

	List<ClassroomEntity> search(String term, Long yearId);

	long countByAcademicYear(Long yearId);

	void delete(Long id);
}
