package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import mz.co.sge.entity.AcademicYearEntity;

public interface IAcademicYearRepository
{

	List<AcademicYearEntity> findAll();

	Optional<AcademicYearEntity> findActive();

	Optional<AcademicYearEntity> findByDescription(String description);

	Optional<AcademicYearEntity> findById(Long id);

	Optional<AcademicYearEntity> findByYear(String year);

	AcademicYearEntity save(AcademicYearEntity entity);

	void delete(Long id);

	Long count();

	List<AcademicYearEntity> findByYearContaining(String year);

	List<AcademicYearEntity> findByYear(int year);
	
}
