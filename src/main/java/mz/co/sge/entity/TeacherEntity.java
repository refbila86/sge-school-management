package mz.co.sge.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@NamedQueries(
{ @NamedQuery(name = "Teacher.findAll", query = "SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.academicYear LEFT JOIN FETCH t.user ORDER BY t.firstName"),
		@NamedQuery(name = "Teacher.findById", query = "SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.academicYear LEFT JOIN FETCH t.user WHERE t.id = :id"),
		@NamedQuery(name = "Teacher.findByAcademicYear", query = "SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects LEFT JOIN FETCH t.academicYear LEFT JOIN FETCH t.user WHERE t.academicYear.id = :yearId ORDER BY t.firstName"),
		@NamedQuery(name = "Teacher.findByUserId", query = "SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects WHERE t.user.id = :userId"),
		@NamedQuery(name = "Teacher.findBySubject", query = "SELECT DISTINCT t FROM TeacherEntity t LEFT JOIN FETCH t.subjects s WHERE s.id = :subjectId") })
public class TeacherEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, length = 20)
	private String code;

	private String firstName;
	private String lastName;
	private String phone;
	private String email;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	@ManyToMany(fetch = FetchType.LAZY) // <--- MUDA PARA LAZY, vamos controlar com FETCH na query
	@JoinTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
	private List<SubjectEntity> subjects = new ArrayList<>();

	@Column(nullable = false)
	private boolean active = true;

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
		return firstName != null ? firstName : "";
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName != null ? lastName : "";
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFullName()
	{
		try
		{
			return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
		} catch (Exception e)
		{
			return code;
		}
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

	public List<SubjectEntity> getSubjects()
	{
		return subjects;
	}

	public void setSubjects(List<SubjectEntity> subjects)
	{
		this.subjects = subjects != null ? subjects : new ArrayList<>();
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}
}