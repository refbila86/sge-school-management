package mz.co.sge.repository;

import mz.co.sge.entity.MonthlyFeeEntity;
import java.util.List;
import java.util.Optional;

public interface IMonthlyFeeRepository
{
	MonthlyFeeEntity save(MonthlyFeeEntity entity);

	List<MonthlyFeeEntity> saveAll(List<MonthlyFeeEntity> entities);

	Optional<MonthlyFeeEntity> findById(Long id);

	Optional<MonthlyFeeEntity> findByReference(String reference);

	List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId);

	List<MonthlyFeeEntity> findByStudentId(Long studentId);

	List<MonthlyFeeEntity> findByYear(Integer year);

	boolean existsByEnrollmentAndMonthYear(Long enrollmentId, Integer month, Integer year);

	long countByEnrollmentId(Long enrollmentId);

	void deleteByEnrollmentId(Long enrollmentId);

	List<MonthlyFeeEntity> findAll();
}
