package mz.co.sge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import mz.co.sge.enumes.Role;

@Entity
@Table(name = "user")
@NamedQueries(
{ @NamedQuery(name = "UserEntity.findByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username"),
		@NamedQuery(name = "UserEntity.existsByUsername", query = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.username = :username"),
		@NamedQuery(name = "UserEntity.findAllOrderByName", query = "SELECT u FROM UserEntity u ORDER BY u.name ASC"),
		@NamedQuery(name = "UserEntity.findByRole", query = "SELECT u FROM UserEntity u WHERE u.role = :role ORDER BY u.name ASC") })
public class UserEntity
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 80, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private Boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
	private java.time.LocalDateTime createdAt;

	@Column(name = "last_login")
	private java.time.LocalDateTime lastLogin;

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

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public Role getRole()
	{
		return role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

	public Boolean getActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}

	public java.time.LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(java.time.LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public java.time.LocalDateTime getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin(java.time.LocalDateTime lastLogin)
	{
		this.lastLogin = lastLogin;
	}

 
}