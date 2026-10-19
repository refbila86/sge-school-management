package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "academic_year")
@NamedQueries(
{ @NamedQuery(name = "AcademicYearEntity.findAll", query = "SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC"),
		@NamedQuery(name = "AcademicYearEntity.findActive", query = "SELECT a FROM AcademicYearEntity a WHERE a.active = true ORDER BY a.year DESC"),
		@NamedQuery(name = "AcademicYearEntity.findByYear", query = "SELECT a FROM AcademicYearEntity a WHERE a.year = :year"),
		@NamedQuery(name = "AcademicYearEntity.findBySchool", query = "SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC") // mantida pra compatibilidade,
																																	// mas agora retorna tudo
})
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

	// getters/setters
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
}