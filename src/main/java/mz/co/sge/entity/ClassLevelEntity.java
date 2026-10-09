package mz.co.sge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_levels", uniqueConstraints = @UniqueConstraint(columnNames =
{ "name", "academic_year_id" }))
@NamedQueries(
{ @NamedQuery(name = "ClassLevel.findAll", query = "SELECT c FROM ClassLevelEntity c ORDER BY c.name"),
		@NamedQuery(name = "ClassLevel.findByAcademicYear", query = "SELECT c FROM ClassLevelEntity c WHERE c.academicYear.id = :yearId ORDER BY c.name") })
public class ClassLevelEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;  

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
		if (createdAt == null)
			createdAt = LocalDateTime.now();
	}

	// Getters e Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
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