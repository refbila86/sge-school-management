package mz.co.sge.repository;

import java.util.List;
import mz.co.sge.entity.ClassLevelEntity;

public interface IClassLevelRepository
{
	ClassLevelEntity save(ClassLevelEntity entity);

	ClassLevelEntity findById(Long id);

	List<ClassLevelEntity> findAll();

	List<ClassLevelEntity> findByAcademicYear(Long yearId);
	
	boolean existsByNameAndYear(String name, Long yearId);

	void delete(Long id);
}