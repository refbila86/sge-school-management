package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import mz.co.sge.enumes.MonthlyFeeStatus;
import mz.co.sge.enumes.PaymentMethod;

@Entity
@Table(name = "monthly_fees", uniqueConstraints =
{ @UniqueConstraint(name = "uk_monthly_fee_reference", columnNames =
{ "reference" }), @UniqueConstraint(name = "uk_enrollment_month_year", columnNames =
{ "enrollment_id", "month", "year" }) })
@NamedQueries(
{ @NamedQuery(name = "MonthlyFeeEntity.findAll", query = "SELECT m FROM MonthlyFeeEntity m ORDER BY m.year DESC, m.month DESC"),
		@NamedQuery(name = "MonthlyFeeEntity.findByEnrollment", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollmentId ORDER BY m.year, m.month"),
		@NamedQuery(name = "MonthlyFeeEntity.findByAcademicYear", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.academicYear.id = :yearId ORDER BY m.student.firstName, m.month"),
		@NamedQuery(name = "MonthlyFeeEntity.findByStudent", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.student.id = :studentId ORDER BY m.year, m.month"),
		@NamedQuery(name = "MonthlyFeeEntity.findByReference", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.reference = :ref"),
		@NamedQuery(name = "MonthlyFeeEntity.findByStudentId", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.student.id = :studentId"),
		@NamedQuery(name = "MonthlyFeeEntity.existsByReference", query = "SELECT COUNT(m) FROM MonthlyFeeEntity m WHERE m.reference = :ref"),
		@NamedQuery(name = "MonthlyFeeEntity.existsByEnrollmentMonthYear", query = "SELECT COUNT(m) FROM MonthlyFeeEntity m WHERE m.enrollment.id = :eid AND m.month = :m AND m.year = :y"),
		@NamedQuery(name = "MonthlyFeeEntity.findByYear", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.year = :year ORDER BY m.year DESC, m.month DESC"),
		@NamedQuery(name = "MonthlyFeeEntity.findPaidByAcademicYear", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.academicYear.id = :yearId AND m.status = mz.co.sge.enumes.MonthlyFeeStatus.PAGO ORDER BY m.year DESC, m.month DESC"),
		@NamedQuery(name = "MonthlyFeeEntity.findPaidByYear", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.year = :year AND m.status = mz.co.sge.enumes.MonthlyFeeStatus.PAGO ORDER BY m.year DESC, m.month DESC"),
		@NamedQuery(name = "MonthlyFeeEntity.findUnpaidByStudentCode", query = "SELECT m FROM MonthlyFeeEntity m WHERE LOWER(m.student.code) = LOWER(:code) AND m.status <> mz.co.sge.enumes.MonthlyFeeStatus.PAGO ORDER BY m.year, m.month"),
		@NamedQuery(name = "MonthlyFeeEntity.findStudentByCode", query = "SELECT s FROM StudentEntity s WHERE LOWER(s.code) = LOWER(:code)") })
public class MonthlyFeeEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "enrollment_id", nullable = false)
	private EnrollmentEntity enrollment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private StudentEntity student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@Column(name = "reference", nullable = false, length = 50, unique = true)
	private String reference;

	@Column(name = "month", nullable = false)
	private Integer month;
	@Column(name = "year", nullable = false)
	private Integer year;
	@Column(name = "month_label", length = 20)
	private String monthLabel;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount = new BigDecimal("2500.00");

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private MonthlyFeeStatus status = MonthlyFeeStatus.PENDENTE;

	@Column(name = "payment_date")
	private LocalDate paymentDate;

	@Column(name = "fine_amount", precision = 10, scale = 2)
	private BigDecimal fineAmount = BigDecimal.ZERO;

	@Column(name = "total_paid", precision = 10, scale = 2)
	private BigDecimal totalPaid = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 30)
	private PaymentMethod paymentMethod;

	@Column(name = "bank_name", length = 100)
	private String bankName;

	@Column(name = "transaction_number", length = 100)
	private String transactionNumber;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id")
	private UserEntity createdBy;

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

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity a)
	{
		this.academicYear = a;
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

	public LocalDate getPaymentDate()
	{
		return paymentDate;
	}

	public void setPaymentDate(LocalDate d)
	{
		this.paymentDate = d;
	}

	public BigDecimal getFineAmount()
	{
		return fineAmount;
	}

	public void setFineAmount(BigDecimal f)
	{
		this.fineAmount = f;
	}

	public BigDecimal getTotalPaid()
	{
		return totalPaid;
	}

	public void setTotalPaid(BigDecimal t)
	{
		this.totalPaid = t;
	}

	public PaymentMethod getPaymentMethod()
	{
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod p)
	{
		this.paymentMethod = p;
	}

	public String getBankName()
	{
		return bankName;
	}

	public void setBankName(String b)
	{
		this.bankName = b;
	}

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public void setTransactionNumber(String t)
	{
		this.transactionNumber = t;
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