package mz.co.sge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import mz.co.sge.enumes.StudentStatus;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@NamedQueries(
{ @NamedQuery(name = "Student.findAll", query = "SELECT s FROM StudentEntity s ORDER BY s.firstName"),
		@NamedQuery(name = "Student.findByCode", query = "SELECT s FROM StudentEntity s WHERE s.code = :code"),
		@NamedQuery(name = "Student.findByStatus", query = "SELECT s FROM StudentEntity s WHERE s.status = :status"),
		@NamedQuery(name = "Student.findByGuardian", query = "SELECT s FROM StudentEntity s WHERE s.guardian.id = :guardianId ORDER BY s.firstName"),
		@NamedQuery(name = "Student.findByAcademicYear", query = "SELECT s FROM StudentEntity s WHERE s.academicYear.id = :academicYearId"),
		@NamedQuery(name = "Student.countAll", query = "SELECT COUNT(s) FROM StudentEntity s"),
		@NamedQuery(name = "Student.countByGuardian", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.guardian.id = :guardianId"),
		@NamedQuery(name = "Student.findBySchoolAndYear", query = "SELECT s FROM StudentEntity s WHERE s.school.id = :schoolId AND s.academicYear.id = :academicYearId ORDER BY s.firstName"),
		@NamedQuery(name = "Student.countByAcademicYear", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.academicYear.id = :academicYearId"),
		@NamedQuery(name = "Student.findBySchool", query = "SELECT s FROM StudentEntity s WHERE s.school.id = :schoolId ORDER BY s.firstName"),
		@NamedQuery(name = "Student.countBySchool", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.school.id = :schoolId"),
		@NamedQuery(name = "Student.countBySchoolAndYear", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.school.id = :schoolId AND s.academicYear.id = :academicYearId") })
public class StudentEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	private LocalDate birthDate;
	private String gender;
	private String phone;
	private String email;
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus status;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "school_id", nullable = false)
	private SchoolEntity school;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "guardian_id", nullable = false)
	private GuardianEntity guardian;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void prePersist()
	{
		if (this.status == null)
			this.status = StudentStatus.REGISTERED;
		if (this.createdAt == null)
			this.createdAt = LocalDateTime.now();
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public LocalDate getBirthDate()
	{
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate)
	{
		this.birthDate = birthDate;
	}

	public String getGender()
	{
		return gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public StudentStatus getStatus()
	{
		return status;
	}

	public void setStatus(StudentStatus status)
	{
		this.status = status;
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

	public GuardianEntity getGuardian()
	{
		return guardian;
	}

	public void setGuardian(GuardianEntity guardian)
	{
		this.guardian = guardian;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public String getFullName()
	{
		return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
	}
}
