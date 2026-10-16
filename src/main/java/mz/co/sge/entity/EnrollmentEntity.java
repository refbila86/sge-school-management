package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@NamedQueries(
{ @NamedQuery(name = "Enrollment.findAll", query = "SELECT e FROM EnrollmentEntity e LEFT JOIN FETCH e.classLevel LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.student LEFT JOIN FETCH e.classroom ORDER BY e.id DESC"),
		@NamedQuery(name = "Enrollment.findByYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId ORDER BY e.id DESC"),
		@NamedQuery(name = "Enrollment.findByStudentAndYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id = :yearId ORDER BY e.id DESC"),
		@NamedQuery(name = "Enrollment.findByStatus", query = "SELECT e FROM EnrollmentEntity e WHERE e.status = :status ORDER BY e.id DESC"),
		@NamedQuery(name = "Enrollment.countByYear", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId"),
		@NamedQuery(name = "Enrollment.countByStudent", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :studentId"),
		// NOVAS QUERIES - FILTRAGEM POR fee_item_id MATRICULA
		@NamedQuery(name = "Enrollment.findStudentsWithMatriculaPaid", query = "SELECT DISTINCT s FROM StudentEntity s WHERE s.school.id = :schoolId AND s.status = 'REGISTERED' "
				+ "AND EXISTS (SELECT 1 FROM PaymentEntity p JOIN p.items pi WHERE p.student.id = s.id AND pi.feeItem.id = :matId) ORDER BY s.firstName"),
		@NamedQuery(name = "Enrollment.findMatriculaFeeItemId", query = "SELECT f.id FROM FeeItemEntity f WHERE f.school.id = :schoolId AND LOWER(f.descricao) LIKE '%matric%' ORDER BY f.id ASC") })
public class EnrollmentEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private SchoolEntity school;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private StudentEntity student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_level_id")
	private ClassLevelEntity classLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "classroom_id")
	private ClassroomEntity classroom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private PaymentEntity payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private UserEntity createdBy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private mz.co.sge.enumes.EnrollmentStatus status;

	@Column(name = "enrollment_date")
	private LocalDateTime enrollmentDate;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void prePersist()
	{
		createdAt = LocalDateTime.now();
		if (enrollmentDate == null)
			enrollmentDate = LocalDateTime.now();
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

	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity school)
	{
		this.school = school;
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

	public ClassroomEntity getClassroom()
	{
		return classroom;
	}

	public void setClassroom(ClassroomEntity classroom)
	{
		this.classroom = classroom;
	}

	public PaymentEntity getPayment()
	{
		return payment;
	}

	public void setPayment(PaymentEntity payment)
	{
		this.payment = payment;
	}

	public UserEntity getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(UserEntity createdBy)
	{
		this.createdBy = createdBy;
	}

	public mz.co.sge.enumes.EnrollmentStatus getStatus()
	{
		return status;
	}

	public void setStatus(mz.co.sge.enumes.EnrollmentStatus status)
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

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}
}
