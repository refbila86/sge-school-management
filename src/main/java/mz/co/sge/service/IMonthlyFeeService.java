package mz.co.sge.service;

import mz.co.sge.entity.MonthlyFeeEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IMonthlyFeeService
{

	// Gera referências de mês inicial até Dezembro - referência =
	// CODIGO_ESTUDANTE-CODIGO_MATRICULA-ANO-MES
	List<MonthlyFeeEntity> generateFromMonth(Long enrollmentId, int startMonth, int year, BigDecimal amount, int dueDay, Long userId);

	List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId);

	MonthlyFeeEntity findByReference(String reference);

	List<MonthlyFeeEntity> findAll();

	void deleteByEnrollmentId(Long enrollmentId);

	long countByEnrollmentId(Long enrollmentId);
}
