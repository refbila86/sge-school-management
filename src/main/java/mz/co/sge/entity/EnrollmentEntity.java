package mz.co.sge.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;

@Entity
@Table(name = "enrollments", uniqueConstraints =
{
		// REGRA DE OURO: impede matricular mesmo aluno 2x no mesmo ano
		@UniqueConstraint(name = "uk_student_academic_year", columnNames =
		{ "student_id", "academic_year_id" }) }, indexes =
{ @Index(name = "idx_enroll_year_status", columnList = "academic_year_id, status"), @Index(name = "idx_enroll_student", columnList = "student_id") })
@NamedQueries(
{ @NamedQuery(name = "Enrollment.findAll", query = "SELECT e FROM EnrollmentEntity e LEFT JOIN FETCH e.classLevel LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.student LEFT JOIN FETCH e.classroom ORDER BY e.id DESC"),
		@NamedQuery(name = "EnrollmentEntity.findByYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId ORDER BY e.id DESC"),
		@NamedQuery(name = "EnrollmentEntity.findByStudentAndYear", query = "SELECT e FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id = :yearId"),
				@NamedQuery(name = "EnrollmentEntity.findByStatus", query = "SELECT e FROM EnrollmentEntity e WHERE e.status = :status ORDER BY e.id DESC"),
		@NamedQuery(name = "EnrollmentEntity.countByYear", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId"),
		@NamedQuery(name = "EnrollmentEntity.countByStudent", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :studentId"),
		@NamedQuery(name = "EnrollmentEntity.findEnrolledStudentIdsByYear", query = "SELECT e.student.id FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId"),
		@NamedQuery(name = "EnrollmentEntity.existsByStudentAndYear", query = "SELECT COUNT(e) > 0 FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id = :yearId"),
		@NamedQuery(name = "EnrollmentEntity.countByClassroomAndYear", query = "SELECT COUNT(e) FROM EnrollmentEntity e " + "WHERE e.classroom.id = :classroomId "
				+ "AND e.academicYear.id = :yearId " + "AND e.status IN ('ENROLLED','PENDING ENROLLMENT')"),
		@NamedQuery(name = "EnrollmentEntity.countPreviousByStudent", query = "SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :studentId AND e.academicYear.id <> :yearId") })

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

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "payment_id")
	private PaymentEntity payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private UserEntity createdBy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnrollmentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnrollmentStatus status;

	@Column(nullable = false, unique = true, length = 25)
	private String code;

	@Column(name = "enrollment_date", nullable = false)
	private LocalDateTime enrollmentDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist()
	{
		LocalDateTime now = LocalDateTime.now();
		if (createdAt == null)
			createdAt = now;
		if (enrollmentDate == null)
			enrollmentDate = now;
		if (status == null)
			status = EnrollmentStatus.PENDING;
		// Se não definiram o tipo, detecta automaticamente
		// Será definido no Service, mas deixamos seguro aqui
	}

	public boolean isRenovation()
	{
		return this.type == EnrollmentType.FIRST_ENROLLMENT;
	}

	public boolean isFirstTime()
	{
		return this.type == EnrollmentType.FIRST_ENROLLMENT;
	}

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

	public EnrollmentType getType()
	{
		return type;
	}

	public void setType(EnrollmentType type)
	{
		this.type = type;
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

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
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