package mz.co.sge.service;

import java.util.List;
import mz.co.sge.entity.ClassLevelEntity;

public interface IClassLevelService
{
	ClassLevelEntity save(ClassLevelEntity entity);

	ClassLevelEntity findById(Long id);

	List<ClassLevelEntity> findAll();

	List<ClassLevelEntity> findByAcademicYear(Long yearId);

	List<ClassLevelEntity> search(String term, Long yearId);

	void delete(Long id);
}