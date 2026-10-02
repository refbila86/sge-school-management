package mz.co.sge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "student_guardians", uniqueConstraints = @UniqueConstraint(columnNames =
{ "student_id", "guardian_id" }))
@NamedQueries(
{ @NamedQuery(name = "StudentGuardian.findByGuardian", query = "SELECT sg FROM StudentGuardianEntity sg WHERE sg.guardian.id = :guardianId AND sg.academicYear.id = :yearId"),
		@NamedQuery(name = "StudentGuardian.findByStudent", query = "SELECT sg FROM StudentGuardianEntity sg WHERE sg.student.id = :studentId"),
		@NamedQuery(name = "StudentGuardian.findStudentsByGuardianUser", query = "SELECT sg.student FROM StudentGuardianEntity sg WHERE sg.guardian.user.id = :userId AND sg.academicYear.id = :yearId") })
public class StudentGuardianEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private StudentEntity student;

	@ManyToOne(optional = false)
	@JoinColumn(name = "guardian_id")
	private GuardianEntity guardian;

	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	private boolean primaryGuardian = false;
	private LocalDateTime createdAt = LocalDateTime.now();

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity student)
	{
		this.student = student;
	}

	public GuardianEntity getGuardian()
	{
		return guardian;
	}

	public void setGuardian(GuardianEntity guardian)
	{
		this.guardian = guardian;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public boolean isPrimaryGuardian()
	{
		return primaryGuardian;
	}

	public void setPrimaryGuardian(boolean primaryGuardian)
	{
		this.primaryGuardian = primaryGuardian;
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
