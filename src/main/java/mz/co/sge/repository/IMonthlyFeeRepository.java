package mz.co.sge.repository;

import java.util.List;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.StudentEntity;

public interface IMonthlyFeeRepository
{
	List<MonthlyFeeEntity> findAll();

	List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId);

	List<MonthlyFeeEntity> findByAcademicYearId(Long yearId);

	List<MonthlyFeeEntity> findByStudentId(Long studentId);

	List<MonthlyFeeEntity> findByYear(Integer year);

	List<MonthlyFeeEntity> findPaidByAcademicYearId(Long yearId);

	List<MonthlyFeeEntity> findPaidByYear(Integer year);

	List<MonthlyFeeEntity> findUnpaidByStudentCode(String code);

	StudentEntity findStudentByCode(String code);

	MonthlyFeeEntity findByReference(String ref);

	boolean existsByReference(String ref);

	boolean existsByEnrollmentMonthYear(Long eid, Integer month, Integer year);

	MonthlyFeeEntity save(MonthlyFeeEntity e);

	MonthlyFeeEntity payFee(Long id);
}