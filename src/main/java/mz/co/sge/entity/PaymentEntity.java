package mz.co.sge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import mz.co.sge.enumes.PaymentStatus;
import mz.co.sge.enumes.PaymentType;

@Entity
@Table(name = "payments")
@NamedQueries(
{ @NamedQuery(name = "Payment.findByStudentAndYear", query = "SELECT p FROM PaymentEntity p WHERE p.student.id = :studentId AND p.academicYear.id = :yearId"),
		@NamedQuery(name = "Payment.findByReference", query = "SELECT p FROM PaymentEntity p WHERE p.reference = :reference") })
public class PaymentEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
	
	@Column(unique = true, nullable = false, length = 40)
	private String reference;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private StudentEntity student;
	
	@Enumerated(EnumType.STRING)
	private PaymentType type;
	
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
	
	private java.math.BigDecimal amount;
	
	private java.time.LocalDateTime paymentDate = java.time.LocalDateTime.now();

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String reference)
	{
		this.reference = reference;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity student)
	{
		this.student = student;
	}

	public PaymentType getType()
	{
		return type;
	}

	public void setType(PaymentType type)
	{
		this.type = type;
	}

	public PaymentStatus getStatus()
	{
		return status;
	}

	public void setStatus(PaymentStatus status)
	{
		this.status = status;
	}

	public java.math.BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(java.math.BigDecimal amount)
	{
		this.amount = amount;
	}

	public java.time.LocalDateTime getPaymentDate()
	{
		return paymentDate;
	}

	public void setPaymentDate(java.time.LocalDateTime paymentDate)
	{
		this.paymentDate = paymentDate;
	}
}