package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_items")
@NamedQueries({
	 @NamedQuery(name = "FeeItem.findAll", query = "SELECT f FROM FeeItemEntity f ORDER BY f.descricao ASC"),
	 @NamedQuery(name = "FeeItem.findBySchool", query = "SELECT f FROM FeeItemEntity f WHERE f.school.id = :schoolId ORDER BY f.descricao ASC"),
	 @NamedQuery(name = "FeeItem.findBySchoolAndYear", query = "SELECT f FROM FeeItemEntity f WHERE f.school.id = :schoolId AND f.academicYear.id = :yearId ORDER BY f.descricao ASC"),
	 @NamedQuery(name = "FeeItem.findActiveBySchool", query = "SELECT f FROM FeeItemEntity f WHERE f.school.id = :schoolId AND f.academicYear.id = :yearId AND f.activo = true ORDER BY f.descricao ASC"),
	 @NamedQuery(name = "FeeItem.findByDescricao", query = "SELECT f FROM FeeItemEntity f WHERE f.school.id = :schoolId AND f.academicYear.id = :yearId AND LOWER(f.descricao) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY f.descricao ASC"),
	 @NamedQuery(name = "FeeItem.countAll", query = "SELECT COUNT(f) FROM FeeItemEntity f"),
	 @NamedQuery(name = "FeeItem.countBySchool", query = "SELECT COUNT(f) FROM FeeItemEntity f WHERE f.school.id = :schoolId")
	})
public class FeeItemEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String descricao; // Inscricao, Matricula, Mensalidade 1a classe

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "school_id")
	private SchoolEntity school;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	@PrePersist
	public void prePersist()
	{
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate()
	{
		updatedAt = LocalDateTime.now();
	}

	// getters/setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getDescricao()
	{
		return descricao;
	}

	public void setDescricao(String d)
	{
		this.descricao = d;
	}

	public BigDecimal getValor()
	{
		return valor;
	}

	public void setValor(BigDecimal v)
	{
		this.valor = v;
	}

	public boolean isActivo()
	{
		return activo;
	}

	public void setActivo(boolean a)
	{
		this.activo = a;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime c)
	{
		this.createdAt = c;
	}

	public LocalDateTime getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime u)
	{
		this.updatedAt = u;
	}

	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity s)
	{
		this.school = s;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity a)
	{
		this.academicYear = a;
	}
}