package mz.co.sge.entity;

import jakarta.persistence.*;
import mz.co.sge.enumes.EnrollmentStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames =
{ "student_id", "academic_year_id" }))
@NamedQueries(
{ @NamedQuery(name = "Enrollment.findAll", query = "SELECT e FROM EnrollmentEntity e ORDER BY e.enrollmentDate DESC"),
		@NamedQuery(name = "Enrollment.findByStudentAndYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id = :yearId"),
		@NamedQuery(name = "Enrollment.findByYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId"),
		@NamedQuery(name = "Enrollment.findByStatus", query = "SELECT e FROM EnrollmentEntity e WHERE e.status = :status"),
		@NamedQuery(name = "Enrollment.countByYear", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId") })
public class EnrollmentEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private StudentEntity student;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "class_level_id", nullable = false)
	private ClassLevelEntity classLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private PaymentEntity payment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnrollmentStatus status = EnrollmentStatus.PENDING;

	@Column(name = "enrollment_date", nullable = false)
	private LocalDateTime enrollmentDate = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id")
	private UserEntity createdBy;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "classroom_id")
	private ClassroomEntity classroom;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist()
	{
		if (createdAt == null)
			createdAt = LocalDateTime.now();
		if (updatedAt == null)
			updatedAt = LocalDateTime.now();
		if (enrollmentDate == null)
			enrollmentDate = LocalDateTime.now();
		if (status == null)
			status = EnrollmentStatus.PENDING;
	}

	@PreUpdate
	void preUpdate()
	{
		updatedAt = LocalDateTime.now();
	}

	// getters/setters
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

	public LocalDateTime getEnrollmentDate()
	{
		return enrollmentDate;
	}

	public void setEnrollmentDate(LocalDateTime enrollmentDate)
	{
		this.enrollmentDate = enrollmentDate;
	}

	public UserEntity getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(UserEntity createdBy)
	{
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public ClassroomEntity getClassroom()
	{
		return classroom;
	}

	public void setClassroom(ClassroomEntity classroom)
	{
		this.classroom = classroom;
	}
}
