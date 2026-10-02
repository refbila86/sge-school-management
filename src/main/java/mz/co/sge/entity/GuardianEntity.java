package mz.co.sge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guardians", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@NamedQueries(
{ @NamedQuery(name = "Guardian.findAll", query = "SELECT g FROM GuardianEntity g ORDER BY g.firstName"),
		@NamedQuery(name = "Guardian.findByCode", query = "SELECT g FROM GuardianEntity g WHERE g.code = :code"),
		@NamedQuery(name = "Guardian.findByUserId", query = "SELECT g FROM GuardianEntity g WHERE g.user.id = :userId"),
		@NamedQuery(name = "Guardian.findWithStudents", query = "SELECT DISTINCT g FROM GuardianEntity g LEFT JOIN FETCH g.students WHERE g.id = :id"),
		@NamedQuery(name = "Guardian.findByAcademicYear", query = "SELECT g FROM GuardianEntity g WHERE g.academicYear.id = :academicYearId"),
		@NamedQuery(name = "Guardian.findBySchool", query = "SELECT DISTINCT g FROM GuardianEntity g JOIN g.students s WHERE s.school.id = :schoolId ORDER BY g.firstName"),
		@NamedQuery(name = "Guardian.findBySchoolAndYear", query = "SELECT DISTINCT g FROM GuardianEntity g JOIN g.students s WHERE s.school.id = :schoolId AND s.academicYear.id = :academicYearId"),
		@NamedQuery(name = "Guardian.countAll", query = "SELECT COUNT(g) FROM GuardianEntity g") })
public class GuardianEntity implements java.io.Serializable
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

	private String phone;
	private String email;
	private String address;
	private String relationship;

	@OneToOne(optional = false)
	@JoinColumn(name = "user_id", unique = true)
	private UserEntity user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	@OneToMany(mappedBy = "guardian", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
	private List<StudentEntity> students = new ArrayList<>();

	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void prePersist()
	{
		if (this.createdAt == null)
			this.createdAt = LocalDateTime.now();
	}

	public void addStudent(StudentEntity student)
	{
		students.add(student);
		student.setGuardian(this);
	}

	public void removeStudent(StudentEntity student)
	{
		students.remove(student);
		student.setGuardian(null);
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

	public String getRelationship()
	{
		return relationship;
	}

	public void setRelationship(String relationship)
	{
		this.relationship = relationship;
	}

	public UserEntity getUser()
	{
		return user;
	}

	public void setUser(UserEntity user)
	{
		this.user = user;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public List<StudentEntity> getStudents()
	{
		return students;
	}

	public void setStudents(List<StudentEntity> students)
	{
		this.students = students;
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
