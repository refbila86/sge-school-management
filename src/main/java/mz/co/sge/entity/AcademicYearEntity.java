package mz.co.sge.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Entity
@Table(name = "academic_year")
@NamedQueries(
{ @NamedQuery(name = "AcademicYearEntity.findAll", query = "SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC"),
		@NamedQuery(name = "AcademicYearEntity.findActive", query = "SELECT a FROM AcademicYearEntity a WHERE a.active = true"),
		@NamedQuery(name = "AcademicYearEntity.findByDescription", query = "SELECT a FROM AcademicYearEntity a WHERE a.year = :year") })
public class AcademicYearEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "`year`", nullable = false, unique = true)
	private String year;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "active")
	private Boolean active = false;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP")
	@CreationTimestamp
	private LocalDateTime createdAt;

	public AcademicYearEntity()
	{
	}

	public AcademicYearEntity(String year, LocalDate startDate, LocalDate endDate, Boolean active)
	{
		this.year = year;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
	}

	// GETTERS E SETTERS NA MÃO - SEM LOMBOK
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

	public LocalDate getStartDate()
	{
		return startDate;
	}

	public void setStartDate(LocalDate startDate)
	{
		this.startDate = startDate;
	}

	public LocalDate getEndDate()
	{
		return endDate;
	}

	public void setEndDate(LocalDate endDate)
	{
		this.endDate = endDate;
	}

	public Boolean getActive()
	{
		return active;
	}

	public void setActive(Boolean active)
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