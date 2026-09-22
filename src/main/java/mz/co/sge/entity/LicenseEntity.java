package mz.co.sge.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.*;

@Entity
@Table(name = "license")
public class LicenseEntity
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", unique = true, nullable = false, length = 50)
	private String code; // Ex: LIC-2026-XPTO

	@Column(name = "type")
	private String type; // BASIC, PRO, ENTERPRISE

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "duration_days")
	private Integer durationDays = 365;

	@Column(name = "active")
	private Boolean active = true;

	// TRANSIENT - calculado
	@Transient
	public long getDaysRegistered()
	{
		if (startDate == null)
			return 0;
		return ChronoUnit.DAYS.between(startDate, LocalDate.now());
	}

	@Transient
	public long getDaysRemaining()
	{
		if (endDate == null)
			return 0;
		long remaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
		return remaining < 0 ? 0 : remaining;
	}

	@Transient
	public boolean isExpired()
	{
		return getDaysRemaining() == 0;
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

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
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

	public Integer getDurationDays()
	{
		return durationDays;
	}

	public void setDurationDays(Integer durationDays)
	{
		this.durationDays = durationDays;
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