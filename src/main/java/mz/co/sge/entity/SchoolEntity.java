package mz.co.sge.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "school")
@NamedQueries(
{ @NamedQuery(name = "SchoolEntity.findByCode", query = "SELECT s FROM SchoolEntity s WHERE s.code = :code"),
		@NamedQuery(name = "SchoolEntity.findByLicenseId", query = "SELECT s FROM SchoolEntity s WHERE s.licenseId = :licenseId"),
		@NamedQuery(name = "SchoolEntity.findAllActiveOrderByNameAsc", query = "SELECT s FROM SchoolEntity s WHERE s.active = true ORDER BY s.name ASC"),
		@NamedQuery(name = "SchoolEntity.existsByCode", query = "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SchoolEntity s WHERE s.code = :code") })
public class SchoolEntity implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "nuit", length = 30)
	private String nuit;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "phone", length = 50)
	private String phone;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "active", nullable = false)
	private Boolean active = true;

	@Column(name = "license_id", nullable = false, unique = true)
	private Long licenseId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate()
	{
		if (this.createdAt == null)
		{
			this.createdAt = LocalDateTime.now();
		}
		if (this.active == null)
		{
			this.active = true;
		}
	}

	public SchoolEntity()
	{
	}

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

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getNuit()
	{
		return nuit;
	}

	public void setNuit(String nuit)
	{
		this.nuit = nuit;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
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

	public Boolean getActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}

	public Long getLicenseId()
	{
		return licenseId;
	}

	public void setLicenseId(Long licenseId)
	{
		this.licenseId = licenseId;
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