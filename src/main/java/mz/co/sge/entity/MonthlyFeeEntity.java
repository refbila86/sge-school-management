//package mz.co.sge.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.NamedQueries;
//import jakarta.persistence.NamedQuery;
//import jakarta.persistence.Table;
//import jakarta.persistence.UniqueConstraint;
//import mz.co.sge.enumes.PaymentStatus;
//
//@Entity
//@Table(name = "monthly_fees", uniqueConstraints = @UniqueConstraint(columnNames =
//{ "reference" }))
//@NamedQueries(
//{ @NamedQuery(name = "MonthlyFee.findByStudentAndYear", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.student.id = :studentId AND m.academicYear.id = :yearId ORDER BY m.month"),
//		@NamedQuery(name = "MonthlyFee.findByReference", query = "SELECT m FROM MonthlyFeeEntity m WHERE m.reference = :reference") })
//public class MonthlyFeeEntity implements java.io.Serializable
//{
//	private static final long serialVersionUID = 1L;
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	@Column(unique = true, nullable = false, length = 50)
//	private String reference;
//
//	@ManyToOne(optional = false)
//	@JoinColumn(name = "academic_year_id")
//	private AcademicYearEntity academicYear;
//
//	@ManyToOne(optional = false)
//	private StudentEntity student;
//
//	@ManyToOne(optional = false)
//	private EnrollmentEntity enrollment;
//
//	private int month;
//
//	private int year;
//
//	private java.math.BigDecimal amount;
//
//	private java.time.LocalDate dueDate;
//
//	@Enumerated(EnumType.STRING)
//	private PaymentStatus status;
//
//	public Long getId()
//	{
//		return id;
//	}
//
//	public void setId(Long id)
//	{
//		this.id = id;
//	}
//
//	public String getReference()
//	{
//		return reference;
//	}
//
//	public void setReference(String reference)
//	{
//		this.reference = reference;
//	}
//
//	public AcademicYearEntity getAcademicYear()
//	{
//		return academicYear;
//	}
//
//	public void setAcademicYear(AcademicYearEntity academicYear)
//	{
//		this.academicYear = academicYear;
//	}
//
//	public StudentEntity getStudent()
//	{
//		return student;
//	}
//
//	public void setStudent(StudentEntity student)
//	{
//		this.student = student;
//	}
//
//	public EnrollmentEntity getEnrollment()
//	{
//		return enrollment;
//	}
//
//	public void setEnrollment(EnrollmentEntity enrollment)
//	{
//		this.enrollment = enrollment;
//	}
//
//	public int getMonth()
//	{
//		return month;
//	}
//
//	public void setMonth(int month)
//	{
//		this.month = month;
//	}
//
//	public int getYear()
//	{
//		return year;
//	}
//
//	public void setYear(int year)
//	{
//		this.year = year;
//	}
//
//	public java.math.BigDecimal getAmount()
//	{
//		return amount;
//	}
//
//	public void setAmount(java.math.BigDecimal amount)
//	{
//		this.amount = amount;
//	}
//
//	public java.time.LocalDate getDueDate()
//	{
//		return dueDate;
//	}
//
//	public void setDueDate(java.time.LocalDate dueDate)
//	{
//		this.dueDate = dueDate;
//	}
//
//	public PaymentStatus getStatus()
//	{
//		return status;
//	}
//
//	public void setStatus(PaymentStatus status)
//	{
//		this.status = status;
//	}
//}