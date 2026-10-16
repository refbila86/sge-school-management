package mz.co.sge.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.enumes.MonthlyFeeStatus;
import mz.co.sge.repository.IMonthlyFeeRepository;
import mz.co.sge.service.IMonthlyFeeService;

@Service
public class MonthlyFeeServiceImpl implements IMonthlyFeeService
{

	@Autowired
	private IMonthlyFeeRepository monthlyFeeRepository;

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public List<MonthlyFeeEntity> generateFromMonth(Long enrollmentId, int startMonth, int year, BigDecimal amount, int dueDay, Long userId)
	{
		EnrollmentEntity enrollment = em.find(EnrollmentEntity.class, enrollmentId);
		if (enrollment == null)
			throw new RuntimeException("Matrícula não encontrada: " + enrollmentId);

		String studentCode = enrollment.getStudent() != null ? enrollment.getStudent().getCode() : "SEM-COD";
		String enrollIdStr = enrollment.getId().toString();

		String[] ptMonths =
		{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };

		List<MonthlyFeeEntity> toSave = new ArrayList<>();

		for (int m = startMonth; m <= 12; m++)
		{
			if (monthlyFeeRepository.existsByEnrollmentAndMonthYear(enrollmentId, m, year))
			{
				continue; // já existe, pula
			}

			String monthStr = String.format("%02d", m);
			// REFERENCIA PEDIDA: codigo estudante + codigo matricula + ano + mes
			String reference = studentCode + "-" + enrollIdStr + "-" + year + "-" + monthStr;

			int day = Math.min(dueDay, LocalDate.of(year, m, 1).lengthOfMonth());
			LocalDate dueDate = LocalDate.of(year, m, day);

			MonthlyFeeEntity fee = new MonthlyFeeEntity();
			fee.setEnrollment(enrollment);
			fee.setStudent(enrollment.getStudent());
			fee.setReference(reference);
			fee.setMonth(m);
			fee.setYear(year);
			fee.setMonthLabel(ptMonths[m - 1]);
			fee.setAmount(amount != null ? amount : new BigDecimal("2500.00"));
			fee.setDueDate(dueDate);
			fee.setStatus(MonthlyFeeStatus.PENDENTE);
			if (userId != null)
			{
				try
				{
					fee.setCreatedBy(em.find(mz.co.sge.entity.UserEntity.class, userId));
				} catch (Exception ignore)
				{
				}
			}

			toSave.add(fee);
		}

		return monthlyFeeRepository.saveAll(toSave);
	}

	@Override
	public List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId)
	{
		return monthlyFeeRepository.findByEnrollmentId(enrollmentId);
	}

	@Override
	public MonthlyFeeEntity findByReference(String reference)
	{
		return monthlyFeeRepository.findByReference(reference).orElse(null);
	}

	@Override
	public List<MonthlyFeeEntity> findAll()
	{
		return monthlyFeeRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteByEnrollmentId(Long enrollmentId)
	{
		monthlyFeeRepository.deleteByEnrollmentId(enrollmentId);
	}

	@Override
	public long countByEnrollmentId(Long enrollmentId)
	{
		return monthlyFeeRepository.countByEnrollmentId(enrollmentId);
	}
}
