package mz.co.sge.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.PaymentMethod;

public interface IMonthlyFeeService
{
	List<MonthlyFeeEntity> findAll();

	List<MonthlyFeeEntity> findByEnrollmentId(Long id);

	List<MonthlyFeeEntity> findPaidByYear(Integer year);

	List<MonthlyFeeEntity> findPaidByAcademicYearId(Long yearId);

	List<MonthlyFeeEntity> findByYear(Integer year);

	List<MonthlyFeeEntity> findUnpaidByStudentCode(String code);

	StudentEntity findStudentByCode(String code);

	List<MonthlyFeeEntity> generateFromMonth(Long enrollmentId, Integer month, Integer year, BigDecimal amount, Integer dueDay, Long userId);

	MonthlyFeeEntity payFee(Long id);	
	
	MonthlyFeeEntity payFee(Long id, PaymentMethod method, String bank, String transNum, LocalDate payDate);

	MonthlyFeeEntity findByReference(String reference);

	void deleteByEnrollmentId(Long enrollmentId);

	long countByEnrollmentId(Long enrollmentId);
	
	List<MonthlyFeeEntity> findByStudentId(Long studentId);
	
	MonthlyFeeEntity payPartial(Long feeId, BigDecimal amountToPay, LocalDate paymentDate);

	MonthlyFeeEntity payPartial(Long feeId, BigDecimal amountToPay, PaymentMethod method, String bank, String transaction, LocalDate paymentDate);


	
}