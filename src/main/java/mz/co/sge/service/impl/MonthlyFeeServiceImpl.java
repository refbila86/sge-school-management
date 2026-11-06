package mz.co.sge.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.enumes.MonthlyFeeStatus;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.repository.IEnrollmentRepository;
import mz.co.sge.repository.IMonthlyFeeRepository;
import mz.co.sge.service.IMonthlyFeeService;

@Service
@Transactional
public class MonthlyFeeServiceImpl implements IMonthlyFeeService
{

	@Autowired
	private IMonthlyFeeRepository monthlyFeeRepository;
	@Autowired
	private IEnrollmentRepository enrollmentRepo;
	@Autowired
	private EntityManager em;

	@Override
	public List<MonthlyFeeEntity> findAll()
	{
		return monthlyFeeRepository.findAll();
	}

	@Override
	public List<MonthlyFeeEntity> findByEnrollmentId(Long id)
	{
		return monthlyFeeRepository.findByEnrollmentId(id);
	}

	@Override
	public List<MonthlyFeeEntity> findPaidByYear(Integer year)
	{
		return monthlyFeeRepository.findPaidByYear(year);
	}

	@Override
	public List<MonthlyFeeEntity> findPaidByAcademicYearId(Long yearId)
	{
		return monthlyFeeRepository.findPaidByAcademicYearId(yearId);
	}

	@Override
	public List<MonthlyFeeEntity> findByYear(Integer year)
	{
		return monthlyFeeRepository.findByYear(year);
	}

	@Override
	public List<MonthlyFeeEntity> findUnpaidByStudentCode(String code)
	{
		return monthlyFeeRepository.findUnpaidByStudentCode(code);
	}

	@Override
	public StudentEntity findStudentByCode(String code)
	{
		return monthlyFeeRepository.findStudentByCode(code);
	}

	@Override
	public MonthlyFeeEntity findByReference(String ref)
	{
		return monthlyFeeRepository.findByReference(ref);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<MonthlyFeeEntity> generateFromMonth(Long enrollmentId, Integer month, Integer year, BigDecimal amount, Integer dueDay, Long userId)
	{
		if (monthlyFeeRepository.existsByEnrollmentMonthYear(enrollmentId, month, year))
			return List.of();
		EnrollmentEntity enr = em.find(EnrollmentEntity.class, enrollmentId);
		if (enr == null)
			return List.of();
		String ref = enr.getStudent().getCode() + "-" + enr.getCode() + "-" + year + "-" + month;
		if (monthlyFeeRepository.existsByReference(ref))
			return List.of();
		MonthlyFeeEntity m = new MonthlyFeeEntity();
		m.setEnrollment(enr);
		m.setStudent(enr.getStudent());
		m.setAcademicYear(enr.getAcademicYear());
		m.setReference(ref);
		m.setMonth(month);
		m.setYear(year);
		m.setMonthLabel(Month.of(month).getDisplayName(TextStyle.FULL, new Locale("pt", "MZ")));
		m.setAmount(amount != null ? amount : new BigDecimal("2500.00"));
		m.setDueDate(LocalDate.of(year, month, dueDay != null ? dueDay : 10));
		m.setStatus(MonthlyFeeStatus.PENDENTE);
		if (userId != null)
			m.setCreatedBy(em.find(UserEntity.class, userId));
		monthlyFeeRepository.save(m);
		return List.of(m);
	}

	@Override
	public MonthlyFeeEntity payFee(Long id, PaymentMethod method, String bank, String transNum, LocalDate payDate)
	{
		MonthlyFeeEntity f = em.find(MonthlyFeeEntity.class, id);
		if (f == null)
			return null;
		f.setPaymentMethod(method);
		f.setBankName(bank);
		f.setTransactionNumber(transNum);
		f.setPaymentDate(payDate != null ? payDate : LocalDate.now());
		return payFee(id);
	}

	@Override
	public void deleteByEnrollmentId(Long enrollmentId)
	{
		em.createQuery("DELETE FROM MonthlyFeeEntity m WHERE m.enrollment.id=:id").setParameter("id", enrollmentId).executeUpdate();
	}

	@Override
	public long countByEnrollmentId(Long enrollmentId)
	{
		return em.createQuery("SELECT COUNT(m) FROM MonthlyFeeEntity m WHERE m.enrollment.id=:id", Long.class).setParameter("id", enrollmentId)
				.getSingleResult();
	}

	@Override
	public MonthlyFeeEntity payFee(Long id)
	{
		MonthlyFeeEntity f = em.find(MonthlyFeeEntity.class, id);
		if (f == null)
			return null;
		if (f.getPaymentDate() == null)
			f.setPaymentDate(LocalDate.now());
		return applyPayment(f);
	}

	private MonthlyFeeEntity applyPayment(MonthlyFeeEntity f)
	{
		if (f.getStatus() == MonthlyFeeStatus.PAGO)
			throw new IllegalStateException("A referência " + f.getReference() + " já foi paga");

		BigDecimal multa = BigDecimal.ZERO;
		if (f.getDueDate() != null && f.getDueDate().isBefore(f.getPaymentDate()))
			multa = f.getAmount().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);

		f.setFineAmount(multa);
		f.setTotalPaid(f.getAmount().add(multa));
		f.setStatus(MonthlyFeeStatus.PAGO);
		return monthlyFeeRepository.save(f);
	}

	@Override
	public List<MonthlyFeeEntity> findByStudentId(Long studentId)
	{
	    return monthlyFeeRepository.findByStudentId(studentId);
	}
}