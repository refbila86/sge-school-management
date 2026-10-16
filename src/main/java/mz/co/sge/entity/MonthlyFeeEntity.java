package mz.co.sge.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import mz.co.sge.enumes.MonthlyFeeStatus;

@Entity
@Table(name = "monthly_fees", uniqueConstraints =
{ @UniqueConstraint(name = "uk_monthly_fee_reference", columnNames =
{ "reference" }), @UniqueConstraint(name = "uk_enrollment_month_year", columnNames =
{ "enrollment_id", "month", "year" }) })
public class MonthlyFeeEntity implements Serializable
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "enrollment_id", nullable = false)
	private EnrollmentEntity enrollment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private StudentEntity student;

	// Referência = CÓDIGO_ESTUDANTE-CÓDIGO_MATRÍCULA-ANO-MÊS ex:
	// EST2026001-45-2026-02
	@Column(name = "reference", nullable = false, length = 80, unique = true)
	private String reference;

	@Column(name = "month", nullable = false)
	private Integer month; // 1-12

	@Column(name = "year", nullable = false)
	private Integer year;

	@Column(name = "month_label", length = 20)
	private String monthLabel; // Janeiro, Fevereiro...

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount = new BigDecimal("2500.00");

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate; // data limite

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private MonthlyFeeStatus status = MonthlyFeeStatus.PENDENTE;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id")
	private UserEntity createdBy;

	public MonthlyFeeEntity()
	{
	}

	public MonthlyFeeEntity(EnrollmentEntity enrollment, String reference, Integer month, Integer year, String monthLabel, BigDecimal amount, LocalDate dueDate)
	{
		this.enrollment = enrollment;
		this.student = enrollment != null ? enrollment.getStudent() : null;
		this.reference = reference;
		this.month = month;
		this.year = year;
		this.monthLabel = monthLabel;
		this.amount = amount;
		this.dueDate = dueDate;
	}

	// Getters Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public EnrollmentEntity getEnrollment()
	{
		return enrollment;
	}

	public void setEnrollment(EnrollmentEntity e)
	{
		this.enrollment = e;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity s)
	{
		this.student = s;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		this.reference = r;
	}

	public Integer getMonth()
	{
		return month;
	}

	public void setMonth(Integer m)
	{
		this.month = m;
	}

	public Integer getYear()
	{
		return year;
	}

	public void setYear(Integer y)
	{
		this.year = y;
	}

	public String getMonthLabel()
	{
		return monthLabel;
	}

	public void setMonthLabel(String l)
	{
		this.monthLabel = l;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal a)
	{
		this.amount = a;
	}

	public LocalDate getDueDate()
	{
		return dueDate;
	}

	public void setDueDate(LocalDate d)
	{
		this.dueDate = d;
	}

	public MonthlyFeeStatus getStatus()
	{
		return status;
	}

	public void setStatus(MonthlyFeeStatus s)
	{
		this.status = s;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime c)
	{
		this.createdAt = c;
	}

	public UserEntity getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(UserEntity u)
	{
		this.createdBy = u;
	}
}
