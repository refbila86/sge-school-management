package mz.co.sge.entity;

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
import mz.co.sge.enumes.EnrollmentStatus;

@Entity
@Table(name = "enrollments")
@NamedQueries(
{ @NamedQuery(name = "Enrollment.findAll", query = "SELECT e FROM EnrollmentEntity e"),
		@NamedQuery(name = "Enrollment.findByStudentAndYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id = :yearId") })
public class EnrollmentEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private StudentEntity student;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "class_level_id")
	private ClassLevelEntity classLevel;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "payment_id")
	private PaymentEntity payment;
	
	@Enumerated(EnumType.STRING)
	private EnrollmentStatus status;
	
	private java.time.LocalDateTime enrollmentDate = java.time.LocalDateTime.now();

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
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

	public ClassLevelEntity getClassLevel()
	{
		return classLevel;
	}

	public void setClassLevel(ClassLevelEntity classLevel)
	{
		this.classLevel = classLevel;
	}

	public PaymentEntity getPayment()
	{
		return payment;
	}

	public void setPayment(PaymentEntity payment)
	{
		this.payment = payment;
	}

	public EnrollmentStatus getStatus()
	{
		return status;
	}

	public void setStatus(EnrollmentStatus status)
	{
		this.status = status;
	}

	public java.time.LocalDateTime getEnrollmentDate()
	{
		return enrollmentDate;
	}

	public void setEnrollmentDate(java.time.LocalDateTime enrollmentDate)
	{
		this.enrollmentDate = enrollmentDate;
	}
}