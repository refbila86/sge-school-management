package mz.co.sge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects", uniqueConstraints = @UniqueConstraint(columnNames =
{ "code" }))
@NamedQueries(
{ @NamedQuery(name = "Subject.findAll", query = "SELECT s FROM SubjectEntity s ORDER BY s.name ASC"),
		@NamedQuery(name = "Subject.findByCode", query = "SELECT s FROM SubjectEntity s WHERE s.code = :code"),
		@NamedQuery(name = "Subject.findByAcademicYear", query = "SELECT s FROM SubjectEntity s WHERE s.academicYear.id = :yearId AND s.active = true"),
		@NamedQuery(name = "Subject.findByName", query = "SELECT s FROM SubjectEntity s WHERE LOWER(s.name) LIKE :name") })
public class SubjectEntity implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String code; // SUB-MAT, SUB-POR

	@Column(nullable = false, length = 100)
	private String name; // Mathematics, Portuguese

	@Column(length = 255)
	private String description;

	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@Column(nullable = false)
	private boolean active = true;

	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void prePersist()
	{
		if (this.createdAt == null)
		{
			this.createdAt = LocalDateTime.now();
		}
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
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