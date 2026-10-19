package mz.co.sge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import mz.co.sge.enumes.StudentStatus;

@Entity
@Table(name = "students", uniqueConstraints =
{ @UniqueConstraint(name = "uk_student_code", columnNames = "code"), @UniqueConstraint(name = "uk_student_document", columnNames = "document_number") })
@NamedQueries(
{ @NamedQuery(name = "Student.findAll", query = "SELECT s FROM StudentEntity s ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.findByCode", query = "SELECT s FROM StudentEntity s WHERE s.code = :code"),
		@NamedQuery(name = "StudentEntity.findByDocument", query = "SELECT s FROM StudentEntity s WHERE s.documentNumber = :doc"),
		@NamedQuery(name = "StudentEntity.findByStatus", query = "SELECT s FROM StudentEntity s WHERE s.status = :status"),
		@NamedQuery(name = "StudentEntity.findByGuardian", query = "SELECT s FROM StudentEntity s WHERE s.guardian.id = :guardianId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.countAll", query = "SELECT COUNT(s) FROM StudentEntity s"),
		@NamedQuery(name = "StudentEntity.countByGuardian", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.guardian.id = :guardianId"),

		// CORRIGIDAS - via enrollments
		@NamedQuery(name = "StudentEntity.findBySchool", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.findBySchoolAndYear", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId AND e.academicYear.id = :academicYearId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.countBySchoolAndYear", query = "SELECT COUNT(DISTINCT s) FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId AND e.academicYear.id = :academicYearId") })
public class StudentEntity implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(name = "document_number", length = 30)
	private String documentNumber; 

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
	@JoinColumn(name = "guardian_id", nullable = false)
	private GuardianEntity guardian;

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy("enrollmentDate DESC")
	private List<EnrollmentEntity> enrollments = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();
	
	

	@PrePersist
	public void prePersist()
	{
		if (this.status == null)
			this.status = StudentStatus.REGISTERED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate()
	{
		this.updatedAt = LocalDateTime.now();
	}

	// MÉTODOS DE NEGÓCIO
	public boolean isFirstEnrollment()
	{
		return enrollments == null || enrollments.isEmpty();
	}

	public String getFullName()
	{
		return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
	}

	// GETTERS E SETTERS
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

	public String getDocumentNumber()
	{
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber)
	{
		this.documentNumber = documentNumber;
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

	public GuardianEntity getGuardian()
	{
		return guardian;
	}

	public void setGuardian(GuardianEntity guardian)
	{
		this.guardian = guardian;
	}

	public List<EnrollmentEntity> getEnrollments()
	{
		return enrollments;
	}

	public void setEnrollments(List<EnrollmentEntity> enrollments)
	{
		this.enrollments = enrollments;
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
}