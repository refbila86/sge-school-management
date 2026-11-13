package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "academic_year")
@NamedQueries(
{ @NamedQuery(name = "AcademicYearEntity.findAll", query = "SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC"),
		@NamedQuery(name = "AcademicYearEntity.findActive", query = "SELECT a FROM AcademicYearEntity a WHERE a.active = true ORDER BY a.year DESC"),
		@NamedQuery(name = "AcademicYearEntity.findByYear", query = "SELECT a FROM AcademicYearEntity a WHERE a.year = :year"),
		@NamedQuery(name = "AcademicYearEntity.findIdByYearValue", query = "SELECT a.id FROM AcademicYearEntity a WHERE a.year = :yearValue"),
		@NamedQuery(name = "AcademicYearEntity.findYearValueById", query = "SELECT a.year FROM AcademicYearEntity a WHERE a.id = :id"),
		@NamedQuery(name = "AcademicYearEntity.findBySchool", query = "SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC") })
public class AcademicYearEntity implements Serializable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 10)
	private String year;

	@Column
	private Boolean active = true;

	@Column(name = "start_date")
	private java.time.LocalDateTime startDate;

	@Column(name = "end_date")
	private java.time.LocalDateTime endDate;

	@Column(name = "created_at")
	private java.time.LocalDateTime createdAt;

	@PrePersist
	public void prePersist()
	{
		if (createdAt == null)
		{
			createdAt = java.time.LocalDateTime.now();
		}
	}
	
	public AcademicYearEntity(Long id)
	{
		this.id = id;
	}

	public AcademicYearEntity()
	{
	}

	// getters/setters existentes...
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getYear()
	{
		return year;
	}

	public void setYear(String year)
	{
		this.year = year;
	}

	public Boolean getActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}

	public boolean isActive()
	{
		return active != null && active;
	}

	public java.time.LocalDateTime getStartDate()
	{
		return startDate;
	}

	public void setStartDate(java.time.LocalDateTime startDate)
	{
		this.startDate = startDate;
	}

	public java.time.LocalDateTime getEndDate()
	{
		return endDate;
	}

	public void setEndDate(java.time.LocalDateTime endDate)
	{
		this.endDate = endDate;
	}

	public java.time.LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(java.time.LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}
}