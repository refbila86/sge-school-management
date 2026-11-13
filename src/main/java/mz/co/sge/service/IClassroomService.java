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

	List<ClassroomEntity> search(String term, Long yearId);

	long countByAcademicYear(Long yearId);

	void delete(Long id);

	List<ClassroomEntity> getAllClassrooms();

	List<ClassroomEntity> findAllActive();

	void incrementStudents(Long classroomId);

	void decrementStudents(Long classroomId);
}