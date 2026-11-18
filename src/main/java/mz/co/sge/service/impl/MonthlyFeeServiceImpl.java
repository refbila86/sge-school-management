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

		// Multa única: se já houver fineAmount gravado usa-o, senão 10% se atrasado
		BigDecimal multa = calcMulta(f, f.getPaymentDate());
		BigDecimal totalDevido = amountOf(f).add(multa);

		f.setFineAmount(multa);
		// Histórico de métodos: acumula, não substitui
		f.addPaymentMethod(f.getPaymentMethod());
		// Neste projeto totalPaid = TOTAL DEVIDO (valor + multa), não o valor pago.
		// O valor pago é o paidAmount — não confundir os dois.
		f.setTotalPaid(totalDevido);
		f.setPaidAmount(totalDevido);
		f.setRemainingAmount(BigDecimal.ZERO);
		f.setStatus(MonthlyFeeStatus.PAGO);
		return monthlyFeeRepository.save(f);
	}

	@Override
	public List<MonthlyFeeEntity> findByStudentId(Long studentId)
	{
		return monthlyFeeRepository.findByStudentId(studentId);
	}

	// ---------- PAGAMENTO PARCIAL ----------
	// Versão curta: delega para a completa, sem forma de pagamento.
	// Antes as duas versões calculavam o total devido de forma DIFERENTE
	// (uma com 10% por LocalDate.now(), outra com totalPaid/fineAmount) — agora há
	// UMA só regra.
	@Override
	@Transactional
	public MonthlyFeeEntity payPartial(Long feeId, BigDecimal amountToPay, LocalDate paymentDate)
	{
		return payPartial(feeId, amountToPay, null, null, null, paymentDate);
	}

	// Versão completa: é ESTA que o bean chama
	// payPartial(id, valor, paymentMethod, bankName, transactionNumber,
	// paymentDate)
	@Override
	@Transactional
	public MonthlyFeeEntity payPartial(Long feeId, BigDecimal amountToPay, PaymentMethod method, String bank, String transaction, LocalDate paymentDate)
	{
		MonthlyFeeEntity fee = em.find(MonthlyFeeEntity.class, feeId);
		if (fee == null)
		{
			throw new RuntimeException("Mensalidade não encontrada: " + feeId);
		}
		if (fee.getStatus() == MonthlyFeeStatus.PAGO)
		{
			throw new RuntimeException("A referência " + fee.getReference() + " já foi paga");
		}
		if (amountToPay == null || amountToPay.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new RuntimeException("Valor a pagar deve ser maior que 0");
		}

		LocalDate dataPag = paymentDate != null ? paymentDate : LocalDate.now();

		// UMA só regra para o total devido: valor + multa (a mesma do bean)
		BigDecimal multa = calcMulta(fee, dataPag);
		BigDecimal totalDevido = amountOf(fee).add(multa);

		BigDecimal jaPago = fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO;
		BigDecimal falta = totalDevido.subtract(jaPago);

		if (amountToPay.compareTo(falta) > 0)
		{
			throw new RuntimeException("Valor não pode ser maior que o remanescente: " + falta + " MT");
		}

		BigDecimal novoPago = jaPago.add(amountToPay);
		BigDecimal novoRemanescente = totalDevido.subtract(novoPago);

		fee.setFineAmount(multa);
		fee.setTotalPaid(totalDevido); // total devido (valor + multa)
		fee.setPaidAmount(novoPago); // o que foi realmente pago
		fee.setRemainingAmount(novoRemanescente.max(BigDecimal.ZERO));

		// NÃO substituir: acumula o novo método ao(s) anterior(es)
		// Ex: 1º pagamento NUMERÁRIO + remanescente MPESA → "NUMERÁRIO, MPESA"
		fee.addPaymentMethod(method);
		fee.setBankName(bank);
		fee.setTransactionNumber(transaction);
		fee.setPaymentDate(dataPag);

		// Liquidou tudo → PAGO (o botão "Pagar remanescente" desaparece), senão →
		// PARCIAL
		fee.setStatus(novoRemanescente.compareTo(BigDecimal.ZERO) <= 0 ? MonthlyFeeStatus.PAGO : MonthlyFeeStatus.PARCIAL);

		return monthlyFeeRepository.save(fee);
	}

	// ---------- REGRAS ÚNICAS (usadas por payFee, payPartial e getTotalWithFine)
	// ----------

	private BigDecimal amountOf(MonthlyFeeEntity fee)
	{
		return fee.getAmount() != null ? fee.getAmount() : BigDecimal.ZERO;
	}

	// Multa: se já houver fineAmount gravado, usa-o; senão 10% se a data de
	// pagamento
	// passou do vencimento. Igual à regra do bean (calcMulta), para o remanescente
	// do bean e da service darem SEMPRE o mesmo valor.
	private BigDecimal calcMulta(MonthlyFeeEntity fee, LocalDate dataPag)
	{
		if (fee.getFineAmount() != null && fee.getFineAmount().compareTo(BigDecimal.ZERO) > 0)
			return fee.getFineAmount();
		LocalDate base = dataPag != null ? dataPag : LocalDate.now();
		if (fee.getDueDate() != null && fee.getDueDate().isBefore(base))
			return amountOf(fee).multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
		return BigDecimal.ZERO;
	}

	private BigDecimal getTotalWithFine(MonthlyFeeEntity fee)
	{
		return amountOf(fee).add(calcMulta(fee, fee.getPaymentDate()));
	}
}
